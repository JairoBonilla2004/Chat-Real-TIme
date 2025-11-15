package ec.edu.espe.chat_real_time.Service.room;


import ec.edu.espe.chat_real_time.Service.HttpRequestService;
import ec.edu.espe.chat_real_time.Service.device.DeviceSessionService;
import ec.edu.espe.chat_real_time.Service.websocket.WebSocketService;
import ec.edu.espe.chat_real_time.dto.mapperDTO.RoomMapper;
import ec.edu.espe.chat_real_time.dto.request.CreateRoomRequest;
import ec.edu.espe.chat_real_time.dto.request.GuestLoginRequest;
import ec.edu.espe.chat_real_time.dto.request.JoinRoomRequest;
import ec.edu.espe.chat_real_time.dto.response.MessageResponse;
import ec.edu.espe.chat_real_time.dto.response.RoomDetailResponse;
import ec.edu.espe.chat_real_time.dto.response.RoomResponse;
import ec.edu.espe.chat_real_time.dto.response.SessionResponse;
import ec.edu.espe.chat_real_time.exception.BadRequestException;
import ec.edu.espe.chat_real_time.exception.ResourceNotFoundException;
import ec.edu.espe.chat_real_time.exception.RoomFullException;
import ec.edu.espe.chat_real_time.model.room.Room;
import ec.edu.espe.chat_real_time.model.user.GuestProfile;
import ec.edu.espe.chat_real_time.model.user.User;
import ec.edu.espe.chat_real_time.model.user.UserSession;
import ec.edu.espe.chat_real_time.repository.MessageRepository;
import ec.edu.espe.chat_real_time.repository.RoomRepository;
import ec.edu.espe.chat_real_time.repository.UserRepository;
import ec.edu.espe.chat_real_time.repository.UserSessionRepository;
import ec.edu.espe.chat_real_time.utils.PinGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

  private final RoomRepository roomRepository;
  private final UserSessionRepository sessionRepository;
  private final MessageRepository messageRepository;
  private final PasswordEncoder passwordEncoder;
  private final PinGenerator pinGeneratorService;
  private final DeviceSessionService deviceSessionService;
  private final HttpRequestService httpRequestService;
  private final WebSocketService webSocketService;
  private final UserRepository userRepository;;

  @Override
  @Transactional
  public RoomResponse createRoom(CreateRoomRequest request, User creator) {
      log.info("Creating room: {} by user: {}", request.getName(), creator.getUsername());

      String roomCode = generateUniqueRoomCode();

      String plainPin = pinGeneratorService.generatePin(4);
      String hashedPin = passwordEncoder.encode(plainPin);


    log.info("Generated PIN for room {}: {}", roomCode, plainPin);

    Room room = Room.builder()
            .roomCode(roomCode)
            .name(request.getName())
            .description(request.getDescription())
            .pinHash(hashedPin)
            .type(request.getType())
            .maxUsers(request.getMaxUsers())
            .currentUsers(0)
            .maxFileSizeMb(request.getMaxFileSizeMb())
            .isActive(true)
            .creator(creator)
            .build();

    room = roomRepository.save(room);
    log.info("Room created successfully: {} with code: {}", room.getName(), room.getRoomCode());

    RoomResponse response = RoomMapper.toRoomResponse(room);
    response.setPlainPin(plainPin);

    return response;
  }

  @Override
  @Transactional
  public RoomDetailResponse joinRoom(JoinRoomRequest request,  HttpServletRequest httpRequest) {

    Room room = roomRepository.findByRoomCodeAndDeletedAtIsNull(request.getRoomCode())
            .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada"));

    if (!room.getIsActive()) {
      throw new BadRequestException("La sala no está activa");
    }

    if (!pinGeneratorService.validatePin(request.getPin(), room.getPinHash())) {

      throw new BadRequestException("PIN incorrecto");
    }

    if (room.isFull()) {
      throw new RoomFullException("La sala está llena");
    }

    String nickname;

    if (request.isAnonymous()){
        nickname = "Anonimo" + UUID.randomUUID().toString().substring(0, 8);
    } else {
        if (request.getNickname() == null || request.getNickname().isBlank()) {
            throw new BadRequestException("Nickname obligatorio");
        }
        nickname = request.getNickname();
    }
      User user = new User();
      user.setUsername("Guest_" + UUID.randomUUID());
      user.setIsActive(true);

    GuestProfile profile = new GuestProfile();
    profile.setNickname(nickname);
    profile.setExpiresAt(LocalDateTime.now().plusHours(24));
    profile.setUser(user);

    user.setGuestProfile(profile);

    User savedUser = userRepository.save(user);

    String deviceId = request.getDeviceId();
      if (deviceId == null || deviceId.isBlank()) {
          deviceId = deviceSessionService.generateDeviceFingerprint(
                  httpRequest.getHeader("User-Agent"),
                  httpRequestService.getClientIpAddress(httpRequest)
          );
      }

      String clientIp = httpRequestService.getClientIpAddress(httpRequest);

      // 7. Registrar sesión dentro de la sala
      UserSession session = UserSession.builder()
              .user(savedUser)
              .room(room)
              .deviceId(deviceId)
              .ipAddress(clientIp)
              .userAgent(httpRequest.getHeader("User-Agent"))
              .isActive(true)
              .build();

      sessionRepository.save(session);

      room.incrementCurrentUsers();
      roomRepository.save(room);

      webSocketService.notifyUserJoinedRoom(room.getId(), savedUser);


      return getRoomDetails(room.getId());
  }

  @Override
  @Transactional
  public void leaveRoom(Long roomId, User user) {
    log.info("User {} leaving room: {}", user.getUsername(), roomId);

    Room room = roomRepository.findByIdAndDeletedAtIsNull(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada"));

    UserSession session = sessionRepository.findByUserAndRoomAndIsActiveTrue(user, room)
            .orElseThrow(() -> new BadRequestException("No estás conectado a esta sala"));

    session.setIsActive(false);
    session.setLeftAt(LocalDateTime.now());
    sessionRepository.save(session);

    room.decrementCurrentUsers();
    roomRepository.save(room);
    webSocketService.notifyUserLeftRoom(roomId, user);
    log.info("User {} left room {} successfully", user.getUsername(), room.getRoomCode());
  }

  @Override
  @Transactional(readOnly = true)
  public RoomResponse getRoomByCode(String roomCode) {
    Room room = roomRepository.findByRoomCodeAndDeletedAtIsNull(roomCode)
            .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada"));

    return RoomMapper.toRoomResponse(room);
  }

  @Override
  @Transactional(readOnly = true)
  public RoomDetailResponse getRoomDetails(Long roomId) {
    Room room = roomRepository.findByIdAndDeletedAtIsNull(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada"));

    List<UserSession> activeSessions = sessionRepository.findByRoomAndIsActiveTrue(room);

    List<SessionResponse> sessionResponses = activeSessions.stream()
            .map(this::mapToSessionResponse)
            .collect(Collectors.toList());

    List<MessageResponse> recentMessages = messageRepository
            .findByRoomAndIsDeletedFalseOrderBySentAtDesc(room)
            .stream()
            .limit(50)
            .map(message -> MessageResponse.builder()
                    .id(message.getId())
                    .content(message.getContent())
                    .messageType(message.getMessageType())
                    .sentAt(message.getSentAt())
                    .isEdited(message.getIsEdited())
                    .editedAt(message.getEditedAt())
                    .senderNickname(message.getSession().getUser().getUsername().startsWith("Guest_") ?
                            message.getSession().getUser().getGuestProfile().getNickname() :
                            message.getSession().getUser().getAdminProfile().getFirstName() + " " + message.getSession().getUser().getAdminProfile().getLastName() + " (Admin) "
                    )
                    .senderId(message.getUser().getId())
                    .roomId(message.getRoom().getId())
                    .build())
            .collect(Collectors.toList());

    return RoomDetailResponse.builder()
            .room(RoomMapper.toRoomResponse(room))
            .activeSessions(sessionResponses)
            .recentMessages(recentMessages)
            .activeUsersCount(activeSessions.size())
            .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<RoomResponse> getAllActiveRooms() {
    return roomRepository.findAllActiveRooms()
            .stream()
            .map(RoomMapper::toRoomResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<RoomResponse> getUserCreatedRooms(User user) {
    return roomRepository.findByCreatorAndDeletedAtIsNull(user)
            .stream()
            .map(RoomMapper::toRoomResponse)
            .collect(Collectors.toList());
  }

  @Override
  public boolean validateRoomPin(String roomCode, String pin) {
    Room room = roomRepository.findByRoomCodeAndDeletedAtIsNull(roomCode)
            .orElseThrow(() -> new ResourceNotFoundException("Sala no encontrada"));

    return passwordEncoder.matches(pin, room.getPinHash());
  }

  private String generateUniqueRoomCode() {
    String roomCode;
    do {
      roomCode = "ROOM" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    } while (roomRepository.existsByRoomCode(roomCode));
    return roomCode;
  }


    private SessionResponse mapToSessionResponse(UserSession session) {
        User u = session.getUser();

        String nicknameInRoom = resolveNickname(session.getUser());


        return SessionResponse.builder()
                .id(session.getId())
                .nicknameInRoom(nicknameInRoom)
                .joinedAt(session.getJoinedAt())
                .isActive(session.getIsActive())
                .ipAddress(session.getIpAddress())
                .build();
    }
    private String resolveNickname(User u) {
        if (u.getGuestProfile() != null) return u.getGuestProfile().getNickname();
        if (u.getAdminProfile() != null)
            return u.getAdminProfile().getFirstName() + " " + u.getAdminProfile().getLastName() + " (Admin)";
        return u.getUsername();
    }



}

