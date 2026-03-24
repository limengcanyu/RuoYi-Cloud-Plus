package org.dromara.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.exception.user.UserException;
import org.dromara.system.api.domain.bo.RemoteUserBo;
import org.dromara.system.api.domain.vo.RemoteUserVo;
import org.dromara.system.api.model.LoginUser;
import org.dromara.system.api.model.XcxLoginUser;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 用户服务
 *
 * @author Lion Li
 */
@FeignClient(contextId = "remoteUserService", name = "ruoyi-system", path = "/remote/user", primary = false)
public interface RemoteUserService {

    /**
     * 通过用户名查询用户信息
     *
     * @param username 用户名
     * @return 结果
     */
    @GetMapping("/get-by-username")
    LoginUser getUserInfo(@RequestParam String username) throws UserException;

    /**
     * 通过用户id查询用户信息
     *
     * @param userId 用户id
     * @return 结果
     */
    @GetMapping("/get-by-id")
    LoginUser getUserInfo(@RequestParam Long userId) throws UserException;

    /**
     * 通过手机号查询用户信息
     *
     * @param phoneNumber 手机号
     * @return 结果
     */
    @GetMapping("/get-by-phonenumber")
    LoginUser getUserInfoByPhoneNumber(@RequestParam String phoneNumber) throws UserException;

    /**
     * 通过邮箱查询用户信息
     *
     * @param email 邮箱
     * @return 结果
     */
    @GetMapping("/get-by-email")
    LoginUser getUserInfoByEmail(@RequestParam String email) throws UserException;

    /**
     * 通过openid查询用户信息
     *
     * @param openid openid
     * @return 结果
     */
    @GetMapping("/get-by-openid")
    XcxLoginUser getUserInfoByOpenid(@RequestParam String openid) throws UserException;

    /**
     * 注册用户信息
     *
     * @param remoteUserBo 用户信息
     * @return 结果
     */
    @PostMapping("/register-user-info")
    Boolean registerUserInfo(@RequestBody RemoteUserBo remoteUserBo) throws UserException, ServiceException;

    /**
     * 通过userId查询用户账户
     *
     * @param userId 用户id
     * @return 结果
     */
    @GetMapping("/select-username-by-id")
    String selectUserNameById(@RequestParam Long userId);

    /**
     * 通过用户ID查询用户昵称
     *
     * @param userId 用户ID
     * @return 用户昵称
     */
    @GetMapping("/select-nickname-by-id")
    String selectNicknameById(@RequestParam Long userId);

    /**
     * 通过用户ID查询用户昵称
     *
     * @param userIds 用户ID 多个用逗号隔开
     * @return 用户昵称
     */
    @GetMapping("/select-nickname-by-ids")
    String selectNicknameByIds(@RequestParam String userIds);

    /**
     * 通过用户ID查询用户手机号
     *
     * @param userId 用户id
     * @return 用户手机号
     */
    @GetMapping("/select-phonenumber-by-id")
    String selectPhonenumberById(@RequestParam Long userId);

    /**
     * 通过用户ID查询用户邮箱
     *
     * @param userId 用户id
     * @return 用户邮箱
     */
    @GetMapping("/select-email-by-id")
    String selectEmailById(@RequestParam Long userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param ip     IP地址
     */
    @PostMapping("/record-login-info")
    void recordLoginInfo(@RequestParam Long userId, @RequestParam String ip);

    /**
     * 通过用户ID查询用户列表
     *
     * @param userIds 用户ids
     * @return 用户列表
     */
    @PostMapping("/select-list-by-ids")
    List<RemoteUserVo> selectListByIds(@RequestBody Collection<Long> userIds);

    /**
     * 通过角色ID查询用户ID
     *
     * @param roleIds 角色ids
     * @return 用户ids
     */
    @PostMapping("/select-user-ids-by-role-ids")
    List<Long> selectUserIdsByRoleIds(@RequestBody Collection<Long> roleIds);

    /**
     * 通过角色ID查询用户
     *
     * @param roleIds 角色ids
     * @return 用户
     */
    @PostMapping("/select-users-by-role-ids")
    List<RemoteUserVo> selectUsersByRoleIds(@RequestBody Collection<Long> roleIds);

    /**
     * 通过部门ID查询用户
     *
     * @param deptIds 部门ids
     * @return 用户
     */
    @PostMapping("/select-users-by-dept-ids")
    List<RemoteUserVo> selectUsersByDeptIds(@RequestBody Collection<Long> deptIds);

    /**
     * 通过岗位ID查询用户
     *
     * @param postIds 岗位ids
     * @return 用户
     */
    @PostMapping("/select-users-by-post-ids")
    List<RemoteUserVo> selectUsersByPostIds(@RequestBody Collection<Long> postIds);

    /**
     * 根据用户 ID 列表查询用户昵称映射关系
     *
     * @param userIds 用户 ID 列表
     * @return Map，其中 key 为用户 ID，value 为对应的用户昵称
     */
    @PostMapping("/select-user-nicks-by-ids")
    Map<Long, String> selectUserNicksByIds(@RequestBody Collection<Long> userIds);

}

