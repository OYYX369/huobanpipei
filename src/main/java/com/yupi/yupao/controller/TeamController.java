package com.yupi.yupao.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.common.BaseResponse;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.common.ResultUtils;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.Team;
import com.yupi.yupao.model.domain.User;
import com.yupi.yupao.model.dto.TeamQuery;
import com.yupi.yupao.model.request.TeamAddRequest;
import com.yupi.yupao.model.request.TeamJoinRequest;
import com.yupi.yupao.model.request.TeamQuitRequest;
import com.yupi.yupao.model.request.TeamUpdateRequest;
import com.yupi.yupao.model.vo.TeamUserVO;
import com.yupi.yupao.service.TeamService;
import com.yupi.yupao.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;


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


    /*
       队长删除队伍
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody Long id , HttpServletRequest request) {
        //检查队伍id合法性
        if(id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"无效的队伍id");
        }
       //获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        //执行删除操作
        boolean result = teamService.deleteTeam(id,loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultUtils.success(result);
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
