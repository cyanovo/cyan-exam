package com.cyan.exam.web.controller.exam;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cyan.exam.common.core.controller.BaseController;
import com.cyan.exam.common.core.domain.AjaxResult;
import com.cyan.exam.system.service.IExamGradingService;

@RestController
@RequestMapping("/exam/grading")
public class TeacherExamController extends BaseController {

    @Autowired
    private IExamGradingService gradingService;

    /**
     * 获取可批改的考试列表（教师视角，目前展示所有考试）
     */
    @PreAuthorize("@ss.hasPermi('exam:grading:list')")
    @GetMapping("/exams")
    public AjaxResult examList() {
        return success(gradingService.getExamListForGrading());
    }

    /**
     * 获取某考试下的学生列表及批改进度
     */
    @PreAuthorize("@ss.hasPermi('exam:grading:list')")
    @GetMapping("/students/{examId}")
    public AjaxResult studentProgress(@PathVariable Long examId) {
        return success(gradingService.getStudentProgress(examId));
    }

    /**
     * 获取某个学生某场考试的答题详情（用于批改）
     */
    @PreAuthorize("@ss.hasPermi('exam:grading:detail')")
    @GetMapping("/detail/{examId}/{userId}")
    public AjaxResult getAnswerDetail(@PathVariable Long examId, @PathVariable Long userId) {
        return success(gradingService.getStudentAnswerDetail(examId, userId));
    }

    /**
     * 提交批改分数（批量保存）
     * 请求体格式：[{ "recordId": 1, "score": 5 }, ...]
     */
    @PreAuthorize("@ss.hasPermi('exam:grading:edit')")
    @PostMapping("/submitScore")
    public AjaxResult submitScore(@RequestBody List<Map<String, Object>> scoreList) {
        gradingService.submitScores(scoreList);
        return success();
    }
}