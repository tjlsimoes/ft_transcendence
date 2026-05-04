package com.codearena.code_arena_backend.user.service;

import com.codearena.code_arena_backend.friendship.entity.Friendship;
import com.codearena.code_arena_backend.friendship.repository.FriendshipRepository;
import com.codearena.code_arena_backend.user.dto.FriendSummaryResponse;
import com.codearena.code_arena_backend.user.dto.UpdateUserProfileRequest;
import com.codearena.code_arena_backend.user.dto.UserAvatarResource;
import com.codearena.code_arena_backend.user.dto.UserProfileResponse;
import com.codearena.code_arena_backend.user.entity.User;
import com.codearena.code_arena_backend.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private static final String FRIENDSHIP_ACCEPTED = "ACCEPTED";
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".gif", ".webp"
    );

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    @Value("${user.avatar.storage-dir:/app/uploads/avatars}")
    private String avatarStorageDir;

    @Value("${user.avatar.base-url:/api/users/avatars}")
    private String avatarBaseUrl;

    @Value("${user.avatar.default-filename:default-avatar.svg}")
    private String defaultAvatarFilename;

    @PostConstruct
    void initAvatarStorage() {
        try {
            Path storagePath = avatarStoragePath();
            Files.createDirectories(storagePath);
            ensureDefaultAvatarExists(storagePath);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize avatar storage", ex);
        }
    }

    public UserProfileResponse getProfileById(Long id) {
        return UserProfileResponse.from(requireUserById(id));
    }

    @Transactional
    public UserProfileResponse updateMyProfile(String username, UpdateUserProfileRequest request) {
        if (request.getDisplayName() == null && request.getBio() == null) {
            throw new IllegalArgumentException("At least one field must be provided: displayName or bio");
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

        return UserProfileResponse.from(userRepository.save(user));
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
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store avatar", ex);
        }

        User user = requireUserByUsername(username);
        user.setAvatar(buildAvatarUrl(filename));
        return UserProfileResponse.from(userRepository.save(user));
    }

    public List<FriendSummaryResponse> listMyFriends(String username) {
        User user = requireUserByUsername(username);
        return listFriendSummaries(user.getId(), false);
    }

    @Transactional
    public void addFriend(String username, Long friendId) {
        User user = requireUserByUsername(username);
        User friend = requireUserById(friendId);

        if (Objects.equals(user.getId(), friend.getId())) {
            throw new IllegalArgumentException("You cannot add yourself as a friend");
        }

        if (!friendshipRepository.existsByUserIdAndFriendId(user.getId(), friend.getId())) {
            friendshipRepository.save(new Friendship(user.getId(), friend.getId(), FRIENDSHIP_ACCEPTED));
        }

        if (!friendshipRepository.existsByUserIdAndFriendId(friend.getId(), user.getId())) {
            friendshipRepository.save(new Friendship(friend.getId(), user.getId(), FRIENDSHIP_ACCEPTED));
        }
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

    private void ensureDefaultAvatarExists(Path storagePath) throws IOException {
        Path defaultAvatarPath = storagePath.resolve(defaultAvatarFilename).normalize();
        if (!defaultAvatarPath.startsWith(storagePath)) {
            throw new IllegalStateException("Invalid default avatar filename");
        }

        if (Files.notExists(defaultAvatarPath)) {
            Files.writeString(
                    defaultAvatarPath,
                    defaultAvatarSvg(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW
            );
        }
    }

    private String defaultAvatarSvg() {
        return """
                <svg xmlns=\"http://www.w3.org/2000/svg\" width=\"256\" height=\"256\" viewBox=\"0 0 256 256\" fill=\"none\">
                  <rect width=\"256\" height=\"256\" rx=\"32\" fill=\"#0A5D73\"/>
                  <circle cx=\"128\" cy=\"96\" r=\"44\" fill=\"#E8F3F7\"/>
                  <path d=\"M52 214c8-40 40-62 76-62s68 22 76 62\" fill=\"#E8F3F7\"/>
                </svg>
                """;
    }
}
