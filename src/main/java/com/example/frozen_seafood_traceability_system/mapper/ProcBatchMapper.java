package com.example.frozen_seafood_traceability_system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.frozen_seafood_traceability_system.entity.ProcBatch;

/**
 * 加工企业产品批号 Mapper。自定义 SQL 见 resources/mapper/ProcBatchMapper.xml。
 */
@Mapper
public interface ProcBatchMapper extends BaseMapper<ProcBatch> {

    /** 本企业批号分页，联出上游（养殖）批号与企业名 */
    IPage<ProcBatch> selectPageForNode(IPage<?> page,
                                       @Param("nodeId") Long nodeId,
                                       @Param("status") Integer status,
                                       @Param("keyword") String keyword);

    /** 详情：按 id 联出上游养殖批号与企业名 */
    ProcBatch selectByIdForNode(@Param("id") Long id);

    /** 养殖端"下游进场确认"待办：进场为指定养殖企业且状态=待确认，可按下游加工企业名模糊 */
    IPage<ProcBatch> waitConfirmList(IPage<?> page,
                                     @Param("inNodeId") Long inNodeId,
                                     @Param("keyword") String keyword);

    /** 级联：某加工企业"已确认"批号下拉 */
    List<ProcBatch> selectSelectableByNode(@Param("nodeId") Long nodeId);
}
