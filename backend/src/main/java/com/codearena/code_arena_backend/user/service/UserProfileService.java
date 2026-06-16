package com.codearena.code_arena_backend.user.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.codearena.code_arena_backend.friendship.dto.FriendNotificationPayload;
import com.codearena.code_arena_backend.friendship.dto.FriendRequestResponse;
import com.codearena.code_arena_backend.friendship.entity.Friendship;
import com.codearena.code_arena_backend.friendship.repository.FriendshipRepository;
import com.codearena.code_arena_backend.notification.NotificationService;
import com.codearena.code_arena_backend.notification.entity.NotificationType;
import com.codearena.code_arena_backend.ranking.service.RankingService;
import com.codearena.code_arena_backend.user.dto.FriendSummaryResponse;
import com.codearena.code_arena_backend.user.dto.UpdatePasswordRequest;
import com.codearena.code_arena_backend.user.dto.UpdateUserProfileRequest;
import com.codearena.code_arena_backend.user.dto.UserAvatarResource;
import com.codearena.code_arena_backend.user.dto.UserProfileResponse;
import com.codearena.code_arena_backend.user.dto.UserSearchResultResponse;
import com.codearena.code_arena_backend.user.entity.RelationshipStatus;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileService {

	private static final String FRIENDSHIP_PENDING = "PENDING";
    private static final String FRIENDSHIP_ACCEPTED = "ACCEPTED";
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".gif", ".webp"
    );

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final PasswordEncoder passwordEncoder;
    private final RankingService rankingService;
	private final NotificationService notificationService;

    @Value("${user.avatar.storage-dir:/app/uploads/avatars}")
    private String avatarStorageDir;

    @Value("${user.avatar.base-url:/api/users/avatars}")
    private String avatarBaseUrl;

    public UserProfileResponse getProfileById(Long id) {
        User user = requireUserById(id);
        return UserProfileResponse.from(user, rankingService.getLeagueFromElo(user.getElo()));
    }

    @Transactional
    public UserProfileResponse updateMyProfile(String username, UpdateUserProfileRequest request) {
        if (request.getDisplayName() == null && request.getBio() == null && request.getEmail() == null) {
            throw new IllegalArgumentException("At least one field must be provided: displayName, bio or email");
        }

        User user = requireUserByUsername(username);

        if (request.getDisplayName() != null) {
            String displayName = request.getDisplayName().trim();
            if (displayName.isEmpty()) {
                throw new IllegalArgumentException("displayName cannot be blank");
            }
            user.setDisplayName(displayName);
        }

        if (request.getBio() != null) {
            String bio = request.getBio().trim();
            user.setBio(bio.isEmpty() ? null : bio);
        }

        if (request.getEmail() != null) {
            String email = request.getEmail().trim();
            if (email.isEmpty()) {
                throw new IllegalArgumentException("Email cannot be blank");
            }
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(email);
        }

        return UserProfileResponse.from(userRepository.save(user), rankingService.getLeagueFromElo(user.getElo()));
    }


    @Transactional
    public void updatePassword(String username, UpdatePasswordRequest request) {
        User user = requireUserByUsername(username);

       if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password");
       }

       String newPassword = request.getNewPassword().trim();
       if (newPassword.isEmpty()) {
           throw new IllegalArgumentException("New password cannot be empty");
       }

       if (newPassword.equals(request.getCurrentPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as current password");
       }

       user.setPassword(passwordEncoder.encode(request.getNewPassword()));
       userRepository.save(user);
    }


    @Transactional
    public UserProfileResponse uploadMyAvatar(String username, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Avatar file is required");
        }

        String filename = generateAvatarFilename(file);
        Path storagePath = avatarStoragePath();
        Path targetPath = storagePath.resolve(filename).normalize();

        if (!targetPath.startsWith(storagePath)) {
            throw new IllegalArgumentException("Invalid avatar filename");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.createDirectories(storagePath);
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store avatar", ex);
        }

        User user = requireUserByUsername(username);
        user.setAvatar(buildAvatarUrl(filename));
        return UserProfileResponse.from(userRepository.save(user), rankingService.getLeagueFromElo(user.getElo()));
    }

    public List<FriendSummaryResponse> listMyFriends(String username) {
        User user = requireUserByUsername(username);
        return listFriendSummaries(user.getId(), false);
    }

    @Transactional
    public void sendFriendRequest(String username, Long targetId) {
		User me = requireUserByUsername(username);
		User target = requireUserById(targetId);

		if (Objects.equals(me.getId(), target.getId())) {
			throw new IllegalArgumentException("You cannot add yourself as a friend");
		}

		Optional<Friendship> outgoing = friendshipRepository.findByUserIdAndFriendId(me.getId(), target.getId());
		if (outgoing.isPresent()) {
			String status = outgoing.get().getStatus();
			if (status.equals(FRIENDSHIP_ACCEPTED)) throw new IllegalArgumentException("You are already friend with this user");
			if (status.equals(FRIENDSHIP_PENDING)) throw new IllegalArgumentException("Friend request already sent");
		}

		Optional<Friendship> incoming = friendshipRepository.findByUserIdAndFriendId(target.getId(), me.getId());
		if (incoming.map(f -> f.getStatus().equals(FRIENDSHIP_PENDING)).orElse(false)) {
			acceptFriendRequest(username, targetId); // they had already requested me: just accept
			return ;
		}

		friendshipRepository.save(new Friendship(me.getId(), target.getId(), FRIENDSHIP_PENDING, LocalDateTime.now()));
		notificationService.send(target.getId(), NotificationType.FRIEND_REQUEST,
				FriendNotificationPayload.builder().userId(me.getId()).username(me.getUsername()).build());
    }

	@Transactional
	public void acceptFriendRequest(String username, Long requesterId) {
		User me = requireUserByUsername(username);

		Friendship pending = friendshipRepository.findByUserIdAndFriendId(requesterId, me.getId())
								.filter(f -> f.getStatus().equals(FRIENDSHIP_PENDING))
								.orElseThrow(() -> new NoSuchElementException("No pending friend request from this user"));
		pending.setStatus(FRIENDSHIP_ACCEPTED);
		friendshipRepository.save(pending);
		friendshipRepository.save(new Friendship(me.getId(), requesterId, FRIENDSHIP_ACCEPTED, LocalDateTime.now()));

		notificationService.send(requesterId, NotificationType.FRIEND_ACCEPTED,
				FriendNotificationPayload.builder().userId(me.getId()).username(me.getUsername()).build());
	}

	@Transactional
	public void rejectFriendRequest(String username, Long requesterId) {
		User me = requireUserByUsername(username);
		Friendship pending = friendshipRepository.findByUserIdAndFriendId(requesterId, me.getId())
								.filter(f -> f.getStatus().equals(FRIENDSHIP_PENDING))
								.orElseThrow(() -> new NoSuchElementException("No pending friend request from this user"));
		friendshipRepository.delete(pending);
	}

    @Transactional
    public void removeFriend(String username, Long friendId) {
        User user = requireUserByUsername(username);
        requireUserById(friendId);

        friendshipRepository.deleteByUserIdAndFriendId(user.getId(), friendId);
        friendshipRepository.deleteByUserIdAndFriendId(friendId, user.getId());
    }

    public List<FriendSummaryResponse> listOnlineFriends(String username) {
        User user = requireUserByUsername(username);
        return listFriendSummaries(user.getId(), true);
    }

	public List<FriendRequestResponse> listPendingRequests(String username) {
		User me = requireUserByUsername(username);
		List<Friendship> pending = friendshipRepository.findPendingForUserId(me.getId());
		if (pending.isEmpty()) return List.of();
		Map<Long, User> usersById = userRepository.findAllById(
			pending.stream().map(Friendship::getUserId).distinct().toList()
			).stream().collect(Collectors.toMap(User::getId, Function.identity()));
		return pending.stream()
				.map(f -> usersById.get(f.getUserId()))
				.filter(Objects::nonNull)
				.map(requester -> new FriendRequestResponse(requester.getId(), requester.getUsername(), requester.getAvatar(),
						pending.stream().filter(f -> f.getUserId().equals(requester.getId())).findFirst().get().getRequestedAt()))
				.sorted(Comparator.comparing(FriendRequestResponse::requestedAt).reversed())
				.toList();
	}


	private RelationshipStatus resolveRelationship(Long me, Long otherId) {
		Optional<Friendship> outgoing = friendshipRepository.findByUserIdAndFriendId(me, otherId);
		Optional<Friendship> incoming = friendshipRepository.findByUserIdAndFriendId(otherId, me);

		if (outgoing.map(f -> f.getStatus().equals(FRIENDSHIP_ACCEPTED)).orElse(false)
				|| incoming.map(f -> f.getStatus().equals(FRIENDSHIP_ACCEPTED)).orElse(false)) {
			return RelationshipStatus.FRIENDS;
		}
		if (outgoing.map(f -> f.getStatus().equals(FRIENDSHIP_PENDING)).orElse(false)) {
			return RelationshipStatus.PENDING_OUTGOING;
		}
		if (incoming.map(f -> f.getStatus().equals(FRIENDSHIP_PENDING)).orElse(false)) {
			return RelationshipStatus.PENDING_INCOMING;
		}
		return RelationshipStatus.NONE;
	}

	public List<UserSearchResultResponse> searchUsers(String username, String query) {
		if (query == null || query.trim().length() < 2) {
			throw new IllegalArgumentException("Search query must be at least 2 chars long");
		}
		User me = requireUserByUsername(username);
		List<User> matches = userRepository.findTop20ByUsernameContainingIgnoreCaseAndIdNot(query.trim(), me.getId());
		return matches.stream()
				.map(u -> UserSearchResultResponse.from(u, resolveRelationship(me.getId(), u.getId())))
				.toList();
	}

    public UserAvatarResource loadAvatar(String filename) {
        Path avatarPath = resolveAvatarPath(filename);

        if (!Files.exists(avatarPath) || !Files.isReadable(avatarPath)) {
            throw new NoSuchElementException("Avatar not found: " + filename);
        }

        try {
            Resource resource = new UrlResource(avatarPath.toUri());
            String contentType = Files.probeContentType(avatarPath);
            MediaType mediaType = contentType == null
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(contentType);

            return new UserAvatarResource(resource, mediaType);
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Failed to resolve avatar path", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to detect avatar content type", ex);
        }
    }

    private List<FriendSummaryResponse> listFriendSummaries(Long userId, boolean onlyOnline) {
        List<Friendship> friendships = friendshipRepository.findByUserIdAndStatus(userId, FRIENDSHIP_ACCEPTED);
        if (friendships.isEmpty()) {
            return List.of();
        }

        List<Long> friendIds = friendships.stream()
                .map(Friendship::getFriendId)
                .distinct()
                .toList();

        Map<Long, User> usersById = userRepository.findAllById(friendIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Stream<FriendSummaryResponse> stream = friendships.stream()
                .map(friendship -> usersById.get(friendship.getFriendId()))
                .filter(Objects::nonNull)
                .map(FriendSummaryResponse::from);

        if (onlyOnline) {
            stream = stream.filter(FriendSummaryResponse::online);
        }

        return stream
                .sorted(Comparator.comparing(FriendSummaryResponse::username, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private User requireUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
    }

    private User requireUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + id));
    }

    private Path avatarStoragePath() {
        return Paths.get(avatarStorageDir).toAbsolutePath().normalize();
    }

    private String buildAvatarUrl(String filename) {
        String normalizedBaseUrl = avatarBaseUrl.endsWith("/")
                ? avatarBaseUrl.substring(0, avatarBaseUrl.length() - 1)
                : avatarBaseUrl;
        return normalizedBaseUrl + "/" + filename;
    }

    private String generateAvatarFilename(MultipartFile file) {
        String extension = extractExtension(file.getOriginalFilename());

        if (extension.isBlank()) {
            extension = extensionFromContentType(file.getContentType());
        }

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Unsupported avatar format. Allowed: .png, .jpg, .jpeg, .gif, .webp"
            );
        }

        return UUID.randomUUID() + extension;
    }

    private String extractExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }

        String cleanedFilename = StringUtils.cleanPath(originalFilename);
        int lastDot = cleanedFilename.lastIndexOf('.');
        if (lastDot < 0) {
            return "";
        }

        return cleanedFilename.substring(lastDot).toLowerCase(Locale.ROOT);
    }

    private String extensionFromContentType(String contentType) {
        if (contentType == null) {
            return "";
        }

        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpeg";
            case "image/jpg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }

    private Path resolveAvatarPath(String filename) {
        String cleanedFilename = StringUtils.cleanPath(filename);

        if (!StringUtils.hasText(cleanedFilename)
                || cleanedFilename.contains("..")
                || cleanedFilename.contains("/")
                || cleanedFilename.contains("\\")) {
            throw new IllegalArgumentException("Invalid avatar filename");
        }

        Path storagePath = avatarStoragePath();
        Path resolvedPath = storagePath.resolve(cleanedFilename).normalize();

        if (!resolvedPath.startsWith(storagePath)) {
            throw new IllegalArgumentException("Invalid avatar filename");
        }

        return resolvedPath;
    }
}
