package com.example.frozen_seafood_traceability_system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.frozen_seafood_traceability_system.entity.WholBatch;

/**
 * 批发商产品批号 Mapper。自定义 SQL 见 resources/mapper/WholBatchMapper.xml。
 */
@Mapper
public interface WholBatchMapper extends BaseMapper<WholBatch> {

    /** 本企业批号分页，联出上游（加工）批号与企业名 */
    IPage<WholBatch> selectPageForNode(IPage<?> page,
                                       @Param("nodeId") Long nodeId,
                                       @Param("status") Integer status,
                                       @Param("keyword") String keyword);

    /** 详情：按 id 联出上游（加工）批号与企业名 */
    WholBatch selectByIdForNode(@Param("id") Long id);

    /** 加工端"下游进场确认"待办：进场为指定加工企业且状态=待确认，可按下游批发商名模糊 */
    IPage<WholBatch> waitConfirmList(IPage<?> page,
                                     @Param("inNodeId") Long inNodeId,
                                     @Param("keyword") String keyword);

    /** 级联：某批发商"已确认"批号下拉 */
    List<WholBatch> selectSelectableByNode(@Param("nodeId") Long nodeId);
}
