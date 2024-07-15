package com.yupi.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupao.common.BaseResponse;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.common.ResultUtils;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.mapper.TeamMapper;
import com.yupi.yupao.model.domain.Team;
import com.yupi.yupao.model.domain.User;
import com.yupi.yupao.model.domain.UserTeam;
import com.yupi.yupao.model.dto.TeamQuery;
import com.yupi.yupao.model.enums.TeamStatusEnum;
import com.yupi.yupao.model.request.TeamJoinRequest;
import com.yupi.yupao.model.request.TeamQuitRequest;
import com.yupi.yupao.model.request.TeamUpdateRequest;
import com.yupi.yupao.model.vo.TeamUserVO;
import com.yupi.yupao.model.vo.UserVO;
import com.yupi.yupao.service.TeamService;
import com.yupi.yupao.service.UserService;
import com.yupi.yupao.service.UserTeamService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author 13303
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-07-06 23:45:40
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;



    /**
     * 添加一个新队伍。
     *
     * @param team      队伍信息。
     * @param loginUser 当前登录的用户。
     * @return 新创建的队伍的ID。
     * @throws BusinessException 业务异常，包括各种参数验证失败或操作不允许。
     */
    @Transactional(rollbackFor = Exception.class)// 开启了事务，当发生任何异常时都进行回滚
    @Override
    public long addTeam(Team team, User loginUser) {
        // 1.验证队伍信息是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.验证是否已登录
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 3.校验信息
        //  a.验证队伍人数是否在1到20之间
        Integer maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }
        //  b.验证队伍名称长度是否超过20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不符合要求");
        }
        //  c.验证队伍描述长度是否超过512字符
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) || description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //  d.验证队伍状态是否有效
        int status = Optional.ofNullable(team.getStatus()).orElse(TeamStatusEnum.PUBLIC.getValue());
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //  e.如果队伍是加密状态，验证密码设置是否正确
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        //  f.验证过期时间是否合理
        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间设置不正确");
        }
        //  g.验证用户创建的队伍数量是否超过限制
        // TODO 有 bug，可能同时创建 100 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        // 4.插入队伍信息
        team.setId(null);
        team.setUserId(loginUser.getId());
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }
        // 5.插入用户-队伍关系
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUser.getId());
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍关系失败");
        }
        return teamId;
    }

    /**
     * 查询队伍列表。
     * 根据指定的查询条件和用户权限，从数据库中获取符合条件的队伍列表。
     *
     * @param teamQuery 查询条件对象，包含了筛选队伍的各种条件。
     * @param isAdmin   当前用户是否为管理员，用于权限判断。
     * @return 包含查询结果的队伍列表。
     * @throws BusinessException 当查询参数异常或权限不足时抛出业务异常。
     */
    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        // 初始化查询条件构造器
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 如果提供了查询条件，构建查询条件
        if (teamQuery != null) {
            // 根据 ID 精确查询
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            // 根据多个 ID 查询
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            // 根据搜索文本模糊查询队伍名称或描述
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            // 根据队伍名称模糊查询
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            // 根据描述模糊查询
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            // 根据最大人数精确查询
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            // 根据用户 ID 精确查询
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据队伍状态进行查询，使用枚举类型确保数据的有效性
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC; // 默认为公开状态
            }
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH, "非管理员无权查看私有队伍");
            }
            queryWrapper.eq("status", statusEnum.getValue());
        }
        // 排除过期的队伍
        // where expireTime > CURRENT_DATE or expireTime is null
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        // 执行查询并获取队伍列表
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>(); // 如果没有查询到任何队伍，返回空列表
        }
        // 关联查询创建人的用户信息并构建返回列表
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO); // 设置创建人信息，脱敏处理
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    //todo  TeamUpdateRequest 添加修改队伍人数
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        //检查队伍对象是否为null
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前的id，验证有效性
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //获取旧的队伍信息
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        //只有管理员或者队伍创建者可以进行更新
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //获取当前 是什么 类型的状态
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        //如果队伍状态为加密，检查是否提供了加密
        if (statusEnum.equals(TeamStatusEnum.SECRET) && StringUtils.isBlank(teamUpdateRequest.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍必须设置密码");
        }
        //创建新的队伍对象并复制属性，准备更新
        Team updateTeam = new Team();
        // 设置属性  相当于代替了 get set方法
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        //执行更新操作，并返回更新结果
        return this.updateById(updateTeam);
    }

    /**
     * 加入队伍操作。
     * 该方法处理用户请求加入一个队伍的业务逻辑。
     * 它会进行多项检查，包括队伍存在性、加入条件（如密码和队伍状态）、以及用户是否已达到加入队伍数量上限。
     *
     * @param teamJoinRequest 包含队伍ID和可能的密码信息的请求对象。
     * @param loginUser       当前登录的用户对象，用于验证和记录谁正在尝试加入队伍。
     * @return 如果加入成功返回true，否则因抛出异常而结束。
     * @throws BusinessException 当输入数据不合法或不满足加入条件时抛出，包括队伍不存在、队伍过期、密码错误、尝试加入私有队伍、用户加入队伍数量超限等情况。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        //确认请求数据的完整性
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //验证队伍id 的 有效性 和获取 队伍信息
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        //验证队伍是否过期
        Date expireTime = team.getExpireTime();
        if (expireTime!=null && expireTime.after(new Date())){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"队伍过期");
        }

        //检查队伍加入权限，私有队伍不允许加入
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"私有队伍不能加入");
        }
        //对于加密队伍，验证密码
        String password = teamJoinRequest.getPassword();
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum) && ( StringUtils.isBlank(password) || !password.equals(team.getPassword()) ) ){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
        }
        // 检查用户 只能加入五个队伍   是否 超过限制
        long userId= loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",userId);
        long number= userTeamService.count(userTeamQueryWrapper);
        if(number>=20){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"最多可以加入5个队伍");
        }
        //检查是否已加入该队伍，防止重复加入
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId",userId);
        userTeamQueryWrapper.eq("teamId",teamId);
        long oncenumber = userTeamService.count(userTeamQueryWrapper);
        if(oncenumber > 0){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"用户已加入该队伍");
        }
        //检查队伍是否已满员，一个队伍不能超过最大限制
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        long Teamusernumber = userTeamService.count(userTeamQueryWrapper);
        if(Teamusernumber >= team.getMaxNum()){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已满");
        }
        //执行加入操作
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());

        return userTeamService.save(userTeam);
    }


    /**
     * 退出队伍操作。
     * 该方法允许用户退出已加入的队伍。它首先验证用户和队伍的有效性，然后根据队伍成员数量处理不同情况：
     * 如果用户是队伍中的唯一成员，则会解散队伍；
     * 如果用户是队长且队伍中还有其他成员，则会将队长职位转移给下一个成员。
     * @param teamQuitRequest 包含队伍ID和必要信息的请求对象。
     * @param loginUser 当前登录的用户信息，用于验证操作权限。
     * @return 返回布尔值，表示退出操作是否成功。
     * @throws BusinessException 抛出业务异常，例如参数错误、未找到队伍、未加入指定队伍、或在队伍操作过程中发生错误。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        //校验参数，确保请求对象不为空
        if(teamQuitRequest ==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //获取并校验队伍id 有效性
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);

        //获取当前登录用户的id
        long userId= loginUser.getId();
        //构建查询条件，检查用户是否为队伍成员
        UserTeam queryuserTeam = new UserTeam();
        queryuserTeam.setUserId(userId);
        queryuserTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryuserTeam);
        long count = userTeamService.count(queryWrapper);
        if(count==0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未加入队伍");
        }
        //获取队伍当前的成员数
        long teamhasjoinnumber = this.countTeamUserByTeamId(teamId);
        if(teamhasjoinnumber == 1){
            //如果队伍只剩一人，则解散队伍
            this.removeById(teamId);
        }else {
            //如果队伍有多于1人
            if(team.getUserId() == userId){
                //如果当前用户是队长，需要转移队长职责
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId",teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList =userTeamService.list(userTeamQueryWrapper);
                //确保有足够的用户列表 进行队长转移
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size()<=1){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                //获取新的队长用户
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextUserTeamUserId = nextUserTeam.getUserId();
                //更新队伍信息，设置新的队长
                Team updateTeam=new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextUserTeamUserId);
                boolean result =this.updateById(updateTeam);
                if(!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队伍失败");
                }
            }
        }
        //移除用户和队伍的关联关系
        return userTeamService.remove(queryWrapper);
    }

    /**
     * 删除指定队伍。
     * 此方法首先检查目标队伍是否存在，然后验证当前登录用户是否为队伍的队长，只有队长有权限删除队伍。
     * 在删除队伍之前，会先删除所有与该队伍相关联的用户关系记录，以确保数据的一致性。
     * @param id 要删除的队伍的ID。
     * @param loginUser 当前登录的用户，用于验证操作权限。
     * @return 删除操作的结果，成功返回true，失败返回false。
     * @throws BusinessException 如果队伍不存在、当前用户不是队伍的队长、或删除操作中出现任何错误，将抛出业务异常。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteTeam(long id, User loginUser) {
        //检查队伍是否存在
        Team team = getTeamById(id);
        long teamId=team.getId();
        // 验证当前登录用户是否为队伍队长
        if(team.getUserId() != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH,"无法访问权限");
        }
        //移出所以与队伍相关联的用户信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId" , teamId);
        boolean result  = userTeamService.remove(userTeamQueryWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联信息失败");
        }
        //删除队伍本身
        return this.removeById(teamId);
    }

    /**
     * 根据队伍ID获取该队伍的当前成员数量。
     * 此方法通过查询特定队伍ID的所有关联用户记录来计算队伍当前的人数。
     * @param teamId 队伍的唯一标识符。
     * @return 返回指定队伍的当前成员数。
     */
    private long countTeamUserByTeamId(long teamId){
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

    /**
     * 根据队伍ID获取队伍信息。
     * 该方法首先检查提供的队伍ID是否有效，然后尝试从数据库中获取队伍信息。
     * 如果队伍ID无效或数据库中不存在该队伍，将抛出业务异常。
     * @param teamId 要获取信息的队伍的ID。
     * @return 返回对应的队伍对象。
     * @throws BusinessException 如果队伍ID无效或队伍不存在，则抛出业务异常。
     */
    private Team getTeamById(Long teamId){
        // 校验队伍ID的有效性
        if(teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.NULL_ERROR, "无效的队伍ID");
        }
        // 获取队伍信息，确认队伍存在
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

}




