package com.example.frozen_seafood_traceability_system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.frozen_seafood_traceability_system.entity.FarmBatch;

/**
 * 养殖企业产品批号 Mapper。自定义 SQL 见 resources/mapper/FarmBatchMapper.xml。
 */
@Mapper
public interface FarmBatchMapper extends BaseMapper<FarmBatch> {

    /** 本企业批号分页（status 为空时排除已下架），联出本企业名 */
    IPage<FarmBatch> selectPageForNode(IPage<?> page,
                                       @Param("nodeId") Long nodeId,
                                       @Param("status") Integer status,
                                       @Param("keyword") String keyword);

    /** 详情：按 id 联出本企业名 */
    FarmBatch selectByIdForNode(@Param("id") Long id);

    /** 级联：某养殖企业"已发布"批号下拉 */
    List<FarmBatch> selectReleasedByNode(@Param("nodeId") Long nodeId);
}
