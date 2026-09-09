package com.example.frozen_seafood_traceability_system.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.frozen_seafood_traceability_system.entity.NodeInfo;

/**
 * 节点企业 Mapper。
 * 自定义 SQL（联表/统计）见 resources/mapper/NodeInfoMapper.xml（docs/开发实施文档.md 附录 D）。
 */
@Mapper
public interface NodeInfoMapper extends BaseMapper<NodeInfo> {

    /** 管理端：分页 + 企业名/类型/省/市 组合模糊，联出中文省市名 */
    IPage<NodeInfo> selectPageWithArea(IPage<?> page,
            @Param("nodeName") String nodeName,
            @Param("nodeType") Integer nodeType,
            @Param("provinceCode") String provinceCode,
            @Param("cityCode") String cityCode);

    /** 级联：按 node_type（+可选省市）查启用中的上游企业列表 */
    List<NodeInfo> upstreamList(@Param("nodeType") Integer nodeType,
            @Param("provinceCode") String provinceCode,
            @Param("cityCode") String cityCode);

    /** 单企业详情：联出省/市中文名（下游新建批号回填 in_area 用） */
    NodeInfo selectByIdWithArea(@Param("id") Long id);

    /** 统计：12 个月注册数量（year 可为 null，缺省当年） */
    List<Map<String, Object>> selectMonthTrend(@Param("year") Integer year);

    /** 统计：按省分组注册数量 */
    List<Map<String, Object>> selectProvinceDist();

    /** 统计：按企业类型分组注册数量 */
    List<Map<String, Object>> selectTypeDist();
}
