package com.cyan.exam.system.service;

import java.util.List;
import com.cyan.exam.system.domain.CyanExamInfo;

public interface ICyanExamInfoService {
    List<CyanExamInfo> selectCyanExamInfoList(CyanExamInfo examInfo);
    CyanExamInfo selectCyanExamInfoByExamId(Long examId);
    int insertCyanExamInfo(CyanExamInfo examInfo);
    int updateCyanExamInfo(CyanExamInfo examInfo);
    int deleteCyanExamInfoByExamIds(Long[] examIds);
}