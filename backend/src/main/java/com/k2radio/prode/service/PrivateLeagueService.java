// src/main/java/com/k2radio/prode/service/PrivateLeagueService.java

package com.k2radio.prode.service;

import com.k2radio.prode.dto.league.*;
import com.k2radio.prode.entity.PrivateLeague;
import com.k2radio.prode.entity.User;
import com.k2radio.prode.exception.*;
import com.k2radio.prode.repository.PrivateLeagueRepository;
import com.k2radio.prode.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivateLeagueService {

    private final PrivateLeagueRepository leagueRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    // ==================== CREAR LIGA ====================

    @Transactional
    public LeagueResponse createLeague(CreateLeagueRequest request, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        String code = generateUniqueCode();

        PrivateLeague league = PrivateLeague.builder()
                .name(request.getName())
                .description(request.getDescription())
                .maxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 50)
                .code(code)
                .owner(owner)
                .build();

        league.getMembers().add(owner);
        owner.getLeagues().add(league);

        league = leagueRepository.save(league);
        log.info("Liga creada: {} (código: {}) por {}", league.getName(), code, ownerEmail);

        return toLeagueResponse(league, owner);
    }

    // ==================== UNIRSE POR CÓDIGO ====================

    @Transactional
    public LeagueResponse joinByCode(String code, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        PrivateLeague league = leagueRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Liga no encontrada con ese código"));

        if (league.getMembers().contains(user)) {
            throw new ConflictException("Ya sos miembro de esta liga");
        }

        if (league.getMembers().size() >= league.getMaxMembers()) {
            throw new BadRequestException("La liga está llena");
        }

        league.getMembers().add(user);
        user.getLeagues().add(league);
        leagueRepository.save(league);

        log.info("Usuario {} se unió a la liga {}", userEmail, league.getName());

        return toLeagueResponse(league, user);
    }

    // ==================== SALIR DE LIGA ====================

    @Transactional
    public void leaveLeague(Long leagueId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        PrivateLeague league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new NotFoundException("Liga no encontrada"));

        if (league.getOwner().getId().equals(user.getId())) {
            throw new BadRequestException("El dueño no puede salir de la liga. Eliminala o transferí la propiedad.");
        }

        if (!league.getMembers().contains(user)) {
            throw new BadRequestException("No sos miembro de esta liga");
        }

        league.getMembers().remove(user);
        user.getLeagues().remove(league);
        leagueRepository.save(league);

        log.info("Usuario {} salió de la liga {}", userEmail, league.getName());
    }

    // ==================== EXPULSAR MIEMBRO ====================

    @Transactional
    public void kickMember(Long leagueId, Long memberId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        PrivateLeague league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new NotFoundException("Liga no encontrada"));

        if (!league.getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException("Solo el dueño puede expulsar miembros");
        }

        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (member.getId().equals(owner.getId())) {
            throw new BadRequestException("No podés expulsarte a vos mismo");
        }

        if (!league.getMembers().contains(member)) {
            throw new BadRequestException("El usuario no es miembro de esta liga");
        }

        league.getMembers().remove(member);
        member.getLeagues().remove(league);
        leagueRepository.save(league);

        log.info("Usuario {} expulsado de la liga {} por {}", memberId, league.getName(), ownerEmail);
    }

    // ==================== ELIMINAR LIGA ====================

    @Transactional
    public void deleteLeague(Long leagueId, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        PrivateLeague league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new NotFoundException("Liga no encontrada"));

        if (!league.getOwner().getId().equals(owner.getId())) {
            throw new ForbiddenException("Solo el dueño puede eliminar la liga");
        }

        for (User member : league.getMembers()) {
            member.getLeagues().remove(league);
        }
        league.getMembers().clear();

        leagueRepository.delete(league);
        log.info("Liga {} eliminada por {}", league.getName(), ownerEmail);
    }

    // ==================== RANKING DE LIGA ====================

    public List<LeagueRankingEntry> getLeagueRanking(Long leagueId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        PrivateLeague league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new NotFoundException("Liga no encontrada"));

        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        boolean isMember = league.getMembers().contains(user);

        if (!isAdmin && !isMember) {
            throw new ForbiddenException("No tenés acceso a esta liga");
        }

        AtomicInteger position = new AtomicInteger(1);

        return league.getMembers().stream()
                .sorted(Comparator
                        .comparingInt(User::getTotalPoints).reversed()
                        .thenComparingInt(User::getExactResults).reversed())
                .map(member -> LeagueRankingEntry.builder()
                        .position(position.getAndIncrement())
                        .userId(member.getId())
                        .userName(member.getName())
                        .avatarUrl(member.getAvatarUrl())
                        .totalPoints(member.getTotalPoints())
                        .exactResults(member.getExactResults())
                        .correctWinners(member.getCorrectWinners())
                        .isOwner(member.getId().equals(league.getOwner().getId()))
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== MIS LIGAS ====================

    public List<LeagueResponse> getMyLeagues(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        return leagueRepository.findByMemberId(user.getId()).stream()
                .map(league -> toLeagueResponse(league, user))
                .collect(Collectors.toList());
    }

    // ==================== DETALLE DE LIGA ====================

    public LeagueResponse getLeague(Long leagueId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        PrivateLeague league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new NotFoundException("Liga no encontrada"));

        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        boolean isMember = league.getMembers().contains(user);

        if (!isAdmin && !isMember) {
            throw new ForbiddenException("No tenés acceso a esta liga");
        }

        return toLeagueResponse(league, user);
    }

    // ==================== INFO PÚBLICA POR CÓDIGO ====================

    public LeagueResponse getLeagueByCode(String code, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        PrivateLeague league = leagueRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Liga no encontrada con ese código"));

        return toLeagueResponse(league, user);
    }

    // ==================== INVITAR POR EMAIL ====================

    @Transactional
    public void inviteByEmail(Long leagueId, InviteByEmailRequest request, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        PrivateLeague league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new NotFoundException("Liga no encontrada"));

        if (!league.getMembers().contains(owner)) {
            throw new ForbiddenException("Solo los miembros pueden invitar");
        }

        emailService.sendLeagueInvitation(owner, league, request.getEmail(), frontendUrl);
        log.info("Invitación enviada a {} para la liga {}", request.getEmail(), league.getName());
    }

    // ==================== ADMIN: TODAS LAS LIGAS ====================

    public List<LeagueResponse> getAllLeagues() {
        return leagueRepository.findAllWithMembers().stream()
                .map(league -> toLeagueResponse(league, league.getOwner()))
                .collect(Collectors.toList());
    }

    // ==================== ADMIN: ELIMINAR CUALQUIER LIGA ====================

    @Transactional
    public void adminDeleteLeague(Long leagueId) {
        PrivateLeague league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new NotFoundException("Liga no encontrada"));

        for (User member : league.getMembers()) {
            member.getLeagues().remove(league);
        }
        league.getMembers().clear();

        leagueRepository.delete(league);
        log.info("Liga {} eliminada por admin", league.getName());
    }

    // ==================== RANKING PÚBLICO DE LIGAS ====================

    public List<LeaguePublicRankingEntry> getPublicLeagueRanking() {
        List<PrivateLeague> leagues = leagueRepository.findAllWithMembersAndOwner();

        AtomicInteger position = new AtomicInteger(1);

        return leagues.stream()
                .map(league -> {
                    int totalPoints = league.getMembers().stream()
                            .mapToInt(User::getTotalPoints)
                            .sum();
                    return LeaguePublicRankingEntry.builder()
                            .id(league.getId())
                            .name(league.getName())
                            .description(league.getDescription())
                            .ownerName(league.getOwner().getName())
                            .memberCount(league.getMembers().size())
                            .totalPoints(totalPoints)
                            .build();
                })
                .sorted(Comparator.comparingInt(LeaguePublicRankingEntry::getTotalPoints).reversed())
                .peek(entry -> entry.setPosition(position.getAndIncrement()))
                .collect(Collectors.toList());
    }

    // ==================== HELPERS ====================

    private String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (leagueRepository.existsByCode(code));
        return code;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private LeagueResponse toLeagueResponse(PrivateLeague league, User currentUser) {
        return LeagueResponse.builder()
                .id(league.getId())
                .name(league.getName())
                .code(league.getCode())
                .description(league.getDescription())
                .maxMembers(league.getMaxMembers())
                .memberCount(league.getMembers().size())
                .ownerName(league.getOwner().getName())
                .ownerId(league.getOwner().getId())
                .createdAt(league.getCreatedAt())
                .isMember(league.getMembers().contains(currentUser))
                .isOwner(league.getOwner().getId().equals(currentUser.getId()))
                .build();
    }
}