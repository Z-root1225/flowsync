package hgc.flowsyncapi.service.impl;

import hgc.flowsyncapi.mapper.*;
import hgc.flowsyncapi.service.OverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class OverviewServiceImpl implements OverviewService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectInfoMapper projectInfoMapper;

    @Autowired
    private TaskInfoMapper taskInfoMapper;

    @Autowired
    private TaskSummaryMapper taskSummaryMapper;

    @Override
    public Map<String, Object> getOverviewStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userMapper.selectCount(null));
        stats.put("projectCount", projectInfoMapper.selectCount(null));
        stats.put("taskCount", taskInfoMapper.selectCount(null));
        stats.put("summaryCount", taskSummaryMapper.selectCount(null));
        return stats;
    }
}
