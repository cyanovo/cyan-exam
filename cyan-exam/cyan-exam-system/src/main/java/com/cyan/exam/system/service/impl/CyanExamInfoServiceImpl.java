package com.cyan.exam.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cyan.exam.system.domain.CyanExamInfo;
import com.cyan.exam.system.mapper.CyanExamInfoMapper;
import com.cyan.exam.system.service.ICyanExamInfoService;

@Service
public class CyanExamInfoServiceImpl implements ICyanExamInfoService {

    @Autowired
    private CyanExamInfoMapper cyanExamInfoMapper;

    @Override
    public List<CyanExamInfo> selectCyanExamInfoList(CyanExamInfo examInfo) {
        return cyanExamInfoMapper.selectCyanExamInfoList(examInfo);
    }

    @Override
    public CyanExamInfo selectCyanExamInfoByExamId(Long examId) {
        return cyanExamInfoMapper.selectCyanExamInfoByExamId(examId);
    }

    @Override
    @Transactional
    public int insertCyanExamInfo(CyanExamInfo examInfo) {
        return cyanExamInfoMapper.insertCyanExamInfo(examInfo);
    }

    @Override
    @Transactional
    public int updateCyanExamInfo(CyanExamInfo examInfo) {
        return cyanExamInfoMapper.updateCyanExamInfo(examInfo);
    }

    @Override
    @Transactional
    public int deleteCyanExamInfoByExamIds(Long[] examIds) {
        return cyanExamInfoMapper.deleteCyanExamInfoByExamIds(examIds);
    }
}