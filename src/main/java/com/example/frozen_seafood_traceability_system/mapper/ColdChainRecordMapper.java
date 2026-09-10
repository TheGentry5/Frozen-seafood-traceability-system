package com.example.frozen_seafood_traceability_system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.frozen_seafood_traceability_system.entity.ColdChainRecord;

/**
 * 冷链温度填报记录 Mapper。自定义 SQL 见 resources/mapper/ColdChainRecordMapper.xml。
 */
@Mapper
public interface ColdChainRecordMapper extends BaseMapper<ColdChainRecord> {

    /** 某批号全部温度记录，按采集时间升序 */
    List<ColdChainRecord> listByBatch(@Param("batchType") Integer batchType,
                                      @Param("batchId") Long batchId);
}
