package com.example.frozen_seafood_traceability_system.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.example.frozen_seafood_traceability_system.common.AuthContext;
import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.BizException;
import com.example.frozen_seafood_traceability_system.common.Principal;
import com.example.frozen_seafood_traceability_system.common.StatusConst;
import com.example.frozen_seafood_traceability_system.common.TokenStore;
import com.example.frozen_seafood_traceability_system.entity.Admin;
import com.example.frozen_seafood_traceability_system.entity.NodeInfo;
import com.example.frozen_seafood_traceability_system.mapper.AdminMapper;
import com.example.frozen_seafood_traceability_system.mapper.NodeInfoMapper;
import com.example.frozen_seafood_traceability_system.service.AuthService;

/**
 * 登录 / 用户共通方法业务实现（docs/开发实施文档.md §8 任务 3）。
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private NodeInfoMapper nodeInfoMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private TokenStore tokenStore;

    // 企业登录
    @Override
    public Map<String, Object> login(String nodeCode, String password) {
        // 根据编号查询企业
        NodeInfo user = nodeInfoMapper.selectOne(new LambdaQueryWrapper<NodeInfo>()
                .eq(NodeInfo::getNodeCode, nodeCode));
        boolean pwdMatched = user != null && password != null && md5(password).equals(user.getPassword());
        if (!pwdMatched) {
            throw new BizException("登录编码或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != StatusConst.NODE_ENABLED) {
            throw new BizException("该企业已被停用");
        }
        user.setPassword(null);

        Principal principal = new Principal();
        principal.setType("NODE");
        principal.setId(user.getId());
        principal.setCode(user.getNodeCode());
        principal.setName(user.getNodeName());
        principal.setNodeType(user.getNodeType());
        String token = tokenStore.create(principal);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return data;
    }

    // 管理员登录
    @Override
    public Map<String, Object> adminLogin(String username, String password) {
        // 根据用户名查询管理员
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        boolean pwdMatched = admin != null && password != null && md5(password).equals(admin.getPassword());
        if (!pwdMatched) {
            throw new BizException("用户名或密码错误");
        }
        Principal principal = new Principal();
        principal.setType("ADMIN");
        principal.setId(admin.getId());
        principal.setCode(admin.getUsername());
        principal.setName("系统管理员");
        String token = tokenStore.create(principal);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("role", "ADMIN");
        data.put("username", admin.getUsername());
        return data;
    }

    // 查询当前登录主体信息
    @Override
    public NodeInfo currentUser() {
        NodeInfo node = requireNode();
        node.setPassword(null);
        return node;
    }

    // 更新密码
    @Override
    public void updatePassword(String oldPwd, String newPwd) {
        NodeInfo node = requireNode();
        boolean pwdMatched = oldPwd != null && md5(oldPwd).equals(node.getPassword());
        if (!pwdMatched) {
            throw new BizException(BizCode.BAD_REQUEST, "原密码不正确");
        }
        if (newPwd == null || newPwd.length() < 6 || newPwd.length() > 20) {
            throw new BizException(BizCode.BAD_REQUEST, "新密码长度须为 6~20 位");
        }
        if (md5(newPwd).equals(node.getPassword())) {
            throw new BizException(BizCode.BAD_REQUEST, "新密码不能与原密码相同");
        }
        NodeInfo update = new NodeInfo();
        update.setId(node.getId());
        update.setPassword(md5(newPwd));
        nodeInfoMapper.updateById(update);
        // 密码已变更：吊销该企业全部在线 token
        tokenStore.removeBySubject("NODE", node.getId());
        AuthContext.clear();
    }

    // 退出登录
    @Override
    public void logout() {
        tokenStore.remove(AuthContext.getToken());
        AuthContext.clear();
    }

    // 校验当前请求为节点企业登录并返回其企业信息
    private NodeInfo requireNode() {
        Principal principal = AuthContext.get();
        if (principal == null || !"NODE".equals(principal.getType())) {
            throw new BizException(BizCode.FORBIDDEN, "请先以节点企业身份登录");
        }
        NodeInfo node = nodeInfoMapper.selectById(principal.getId());
        if (node == null) {
            throw new BizException(BizCode.UNAUTHORIZED, "企业不存在");
        }
        return node;
    }

    // md5密码加密
    private static String md5(String s) {
        return DigestUtils.md5DigestAsHex(s.getBytes(StandardCharsets.UTF_8));
    }
}
