package mathrone.backend.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import mathrone.backend.controller.dto.chapter.ChapterDto;
import mathrone.backend.controller.dto.chapter.ChapterGroup;
import mathrone.backend.domain.ChapterInfo;
import mathrone.backend.error.exception.CustomException;
import mathrone.backend.error.exception.ErrorCode;
import mathrone.backend.repository.ChapterRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterRepository chapterRepository;

    public ChapterInfo getChapter(String chapterId) {
        return chapterRepository.findByChapterId(chapterId).orElseThrow(() -> new CustomException(
            ErrorCode.NOT_FOUND_CHAPTER));
    }

    public Set<ChapterGroup> getChapterGroup(Optional<String> isGroup) {
        Set<ChapterGroup> chapterGroups = new HashSet<>();
        if (isGroup.isEmpty()){
            Map<String, Set<ChapterDto>> groups = new HashMap<>();

            for (ChapterInfo chapater : chapterRepository.findAll()){
                ChapterDto c = ChapterDto.builder()
                    .id(chapater.getChapterId())
                    .name(chapater.getName())
                    .build();

                if (groups.containsKey(chapater.getGroup())){
                    groups.get(chapater.getGroup()).add(c);
                } else {
                    Set<ChapterDto> chapters = new HashSet<>(Collections.singleton(c));
                    chapters.add(c);
                    groups.put(chapater.getGroup(), chapters);
                }
            }

            for (Entry<String, Set<ChapterDto>> entry : groups.entrySet()){
                chapterGroups.add(
                    ChapterGroup.builder()
                        .group(entry.getKey())
                        .chapters(entry.getValue())
                        .build()
                );
            }
        } else {
            Set<ChapterInfo> chapters = chapterRepository.findByGroup(isGroup.get());
            Set<ChapterDto> chapterDtos = new HashSet<>();

            for (ChapterInfo c : chapters){
                chapterDtos.add(ChapterDto.builder()
                    .id(c.getChapterId())
                    .name(c.getName())
                    .build());
            }
            chapterGroups.add(ChapterGroup.builder()
                .group(isGroup.get())
                .chapters(chapterDtos)
                .build());

        }
        return chapterGroups;
    }
}
