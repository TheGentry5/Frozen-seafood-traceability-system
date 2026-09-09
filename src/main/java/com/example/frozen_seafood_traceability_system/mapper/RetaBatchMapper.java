package com.example.frozen_seafood_traceability_system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.frozen_seafood_traceability_system.entity.RetaBatch;

/**
 * 零售商产品批号 Mapper。自定义 SQL 见 resources/mapper/RetaBatchMapper.xml。
 */
@Mapper
public interface RetaBatchMapper extends BaseMapper<RetaBatch> {

    /** 本企业批号分页，联出上游（批发）批号与企业名 */
    IPage<RetaBatch> selectPageForNode(IPage<?> page,
                                       @Param("nodeId") Long nodeId,
                                       @Param("status") Integer status,
                                       @Param("keyword") String keyword);

    /** 详情：按 id 联出上游（批发）批号与企业名 */
    RetaBatch selectByIdForNode(@Param("id") Long id);

    /** 批发端"下游进场确认"待办：进场为指定批发商且状态=待确认 */
    IPage<RetaBatch> waitConfirmList(IPage<?> page,
                                     @Param("inNodeId") Long inNodeId,
                                     @Param("keyword") String keyword);

    /** 级联：某零售商无可选（终端环节），本方法预留 */
    List<RetaBatch> selectSelectableByNode(@Param("nodeId") Long nodeId);

    /** 消费者溯源：按溯源码查零售批号（联出批发企业名） */
    RetaBatch selectByTraceCode(@Param("traceCode") String traceCode);
}
