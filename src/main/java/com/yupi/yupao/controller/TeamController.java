package com.yupi.yupao.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.common.BaseResponse;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.common.ResultUtils;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.Team;
import com.yupi.yupao.model.domain.User;
import com.yupi.yupao.model.domain.UserTeam;
import com.yupi.yupao.model.dto.TeamQuery;
import com.yupi.yupao.model.request.*;
import com.yupi.yupao.model.vo.TeamUserVO;
import com.yupi.yupao.service.TeamService;
import com.yupi.yupao.service.UserService;
import com.yupi.yupao.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 *
 */

@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173/"}, allowCredentials = "true")
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;


    @Resource
    private UserTeamService userTeamService;


    /**
     * 获取最匹配的用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, user));
    }

    /*
       队长删除队伍
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        //检查队伍id合法性
        if(deleteRequest ==null || deleteRequest.getId() <=0 ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
       //获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        //执行删除操作
        boolean result = teamService.deleteTeam(id,loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultUtils.success(true);
    }



    /*
            推出队伍
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        // 校验请求体是否为空
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求体不能为空");
        }
        // 从会话中获取当前登录的用户信息
        User loginUser = userService.getLoginUser(request);
        // 调用业务层执行退出队伍的操作
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        // 根据业务层返回的结果，生成并返回响应体
        return ResultUtils.success(result);
    }

    /**
     * 添加队伍接口。
     * 向数据库中插入一个新的队伍记录，并返回其主键 ID。
     *
     * @return 包含添加结果的统一响应结构。
     * @throws BusinessException 当请求参数异常或插入失败时抛出业务异常。
     */

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if(teamAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        //执行插入操作
        Team team=new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        long teamId = teamService.addTeam(team,loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request){
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        //执行插入操作
        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if(!result){
            throw  new BusinessException(ErrorCode.SYSTEM_ERROR,"插入失败");
        }
        return ResultUtils.success(true);
    }




    /**
     * 加入队伍接口。
     * 用于处理用户的加入队伍请求，根据提供的队伍加入信息，将登录用户添加到指定的队伍中。
     * @param teamJoinRequest 包含队伍加入信息的请求对象。
     * @param request HTTP请求对象，用于获取当前请求的用户信息。
     * @return 包含加入操作结果的统一响应结构。返回true表示加入成功，false表示加入失败。
     * @throws BusinessException 当请求参数异常或加入操作失败时抛出业务异常。
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        // 参数校验，确保传入的请求体不为空
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加入队伍请求参数不能为空");
        }
        // 从HTTP请求中获取当前登录的用户信息
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        // 调用服务层方法处理加入队伍的请求
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        // 根据服务层返回的结果，构造响应体
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入队伍失败");
        }
        // 如果加入成功，返回成功的响应
        return ResultUtils.success(result);
    }


    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id){
        if(id <=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if(team == null){
            throw  new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    /**
     * 查询队伍列表接口。
     * 根据条件查询数据库中符合条件的队伍列表。
     * @param teamQuery 查询条件。
     * @return 包含查询结果的统一响应结构。
     * @throws BusinessException 当请求参数异常时抛出业务异常。
     */

    /**
     * 查询队伍列表接口。
     * 根据条件查询数据库中符合条件的队伍列表。
     * @param teamQuery 查询条件。
     * @param request HTTP请求对象，用于获取当前请求的相关信息。
     * @return 包含查询结果的统一响应结构。
     * @throws BusinessException 当请求参数异常时抛出业务异常。
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        // 参数校验
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断当前用户是否为管理员
        boolean isAdmin = userService.isAdmin(request);
        // 调用队伍服务查询队伍列表
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        // 从返回的队伍列表中提取队伍ID
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        // 查询当前登录用户已加入的队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 获取已加入队伍的ID集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            // 标注每个队伍对象用户是否已加入
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
            // 异常处理，此处省略具体实现
        }
        // 3、查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        return ResultUtils.success(teamList);
    }
    /**
     * 获取当前用户创建的队伍列表。
     * 此接口根据当前登录用户的 ID 和提供的查询条件来检索队伍列表。
     * 如果当前用户是管理员，可以检索到更广泛的数据。
     * @param teamQuery 查询条件，包括可以筛选的各种字段。
     * @param request HTTP请求对象，用于获取当前请求的用户会话。
     * @return 返回当前用户创建的队伍列表的统一响应结构。
     * @throws BusinessException 当请求参数为 null 或其他业务规则不满足时抛出。
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request){
        // 参数校验，确保传入的查询条件不为null
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 从会话中获取当前登录的用户信息
        User loginUser = userService.getLoginUser(request);
        // 设置查询条件中的用户ID为当前登录用户的ID，以确保只查询当前用户创建的队伍
        teamQuery.setUserId(loginUser.getId());
        // 调用服务层方法，传入查询条件和是否管理员标志，获取符合条件的队伍列表
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        // 封装并返回查询结果
        return ResultUtils.success(teamList);
    }

    /**
     * 获取我加入的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // 取出不重复的队伍 id
        // teamId userId
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamList);
    }

    /**
     * 分页查询队伍列表接口。
     * 根据条件分页查询数据库中符合条件的队伍列表。
     * @param teamQuery 查询条件。
     * @return 包含查询结果的统一响应结构。
     * @throws BusinessException 当请求参数异常时抛出业务异常。
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamByPage(TeamQuery teamQuery){
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page,queryWrapper);
        return ResultUtils.success(resultPage);
    }

}
