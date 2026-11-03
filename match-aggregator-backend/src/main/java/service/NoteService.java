package ru.mirea.matchaggregator.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.matchaggregator.domain.Match;
import ru.mirea.matchaggregator.domain.Note;
import ru.mirea.matchaggregator.domain.UserAccount;
import ru.mirea.matchaggregator.dto.note.NoteCreateRequest;
import ru.mirea.matchaggregator.dto.note.NoteUpdateRequest;
import ru.mirea.matchaggregator.error.NotFoundException;
import ru.mirea.matchaggregator.repository.NoteRepository;
import ru.mirea.matchaggregator.repository.UserAccountRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final MatchService matchService;
    private final UserAccountRepository userRepository;

    public NoteService(NoteRepository noteRepository,
                       MatchService matchService,
                       UserAccountRepository userRepository) {
        this.noteRepository = noteRepository;
        this.matchService = matchService;
        this.userRepository = userRepository;
    }

    public List<Note> findByMatch(UUID matchId) {
        // Проверяем, что матч существует
        matchService.getById(matchId);
        return noteRepository.findByMatchIdOrderByPinnedDescCreatedAtAsc(matchId);
    }

    @Transactional
    public Note create(UUID matchId, NoteCreateRequest req) {
        Match match = matchService.getById(matchId);
        UserAccount author = getCurrentUser();

        Note note = new Note();
        note.setMatch(match);
        note.setAuthor(author);
        note.setContent(req.content());
        note.setPinned(req.pinned());
        return noteRepository.save(note);
    }

    @Transactional
    public Note update(UUID noteId, NoteUpdateRequest req) {
        Note note = getById(noteId);
        checkOwnershipOrAdmin(note);
        note.setContent(req.content());
        note.setPinned(req.pinned());
        note.setUpdatedAt(Instant.now());
        return noteRepository.save(note);
    }

    @Transactional
    public void delete(UUID noteId) {
        Note note = getById(noteId);
        checkOwnershipOrAdmin(note);
        noteRepository.delete(note);
    }

    private Note getById(UUID id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Заметка не найдена: " + id));
    }

    private UserAccount getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Текущий пользователь не найден"));
    }

    private void checkOwnershipOrAdmin(Note note) {
        UserAccount current = getCurrentUser();
        boolean isAdmin = current.getRole().name().equals("ADMIN");
        boolean isAuthor = note.getAuthor().getId().equals(current.getId());
        if (!isAdmin && !isAuthor) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Редактировать или удалять заметку может только автор или администратор");
        }
    }
}
