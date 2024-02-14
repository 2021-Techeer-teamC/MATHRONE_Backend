package mathrone.backend.controller;

import static org.springframework.http.HttpStatus.OK;

import java.util.Optional;
import java.util.Set;
import mathrone.backend.controller.dto.chapter.ChapterGroup;
import mathrone.backend.domain.ChapterInfo;
import mathrone.backend.service.ChapterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chapter")
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @GetMapping("/{chapterId}")
    public ResponseEntity<ChapterInfo> getChapter(
        @PathVariable(value = "chapterId") String chapterId) {
        return ResponseEntity.status(OK)
            .body(chapterService.getChapter(chapterId));
    }

    @GetMapping("/group")
    public ResponseEntity<Set<ChapterGroup>> getChapterGroup(
        @RequestParam(value = "groupName", required = false) Optional<String> groupName) {
        return ResponseEntity.status(OK)
            .body(chapterService.getChapterGroup(groupName));
    }
}
