package mathrone.backend.controller;

import com.google.api.gax.paging.Page;
import jnr.ffi.annotations.In;
import mathrone.backend.domain.*;
import mathrone.backend.service.WorkBookService;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.criterion.Order;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;//자료형 때문에 오류였음.. awt.print.Pageable아님
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;



import java.util.*;

@RestController
@RequestMapping("/book")
public class WorkbookController {


    private final WorkBookService workBookService;

    public WorkbookController(WorkBookService workBookService){
        this.workBookService = workBookService;
    }


    //workbook API
    @GetMapping("/workbook") // 모든 워크북 조회(Books page)
    public List<bookItem> bookList(@RequestParam(value="publisher", required = false, defaultValue = "all") String publisher,
                                   @RequestParam(value="sortType", required = false, defaultValue = "star") String sortType,
                                   @RequestParam(value="category", required = false, defaultValue = "all") String category,
                                   @RequestParam(value="pageNum", required = false, defaultValue = "1") Integer pageNum){

        Pageable paging = PageRequest.of(pageNum-1,9,Sort.by("workbookId")); //page 0부터임!
        return workBookService.getBookList(paging,publisher,category,sortType);
    }


    @GetMapping("/workbook/info") // 모든 워크북 조회(Books page)
    public Long bookCount(@RequestParam(value="publisher", required = false, defaultValue = "all") String publisher,
                         @RequestParam(value="category", required = false, defaultValue = "all") String category)
    {
        //결과의 수 반환
        return workBookService.countWorkbook(publisher,category);
    }

    
    @GetMapping("workbook/list")
    public List<bookContent> workbookList(){
        return workBookService.getWorkbookList();
    }

}
