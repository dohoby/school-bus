/**
 * @program school-bus
 * @description: BusController
 * @author: mf
 * @create: 2020/03/01 22:56
 */

package com.stylefeng.guns.rest.modular.bus;

import com.alibaba.dubbo.config.annotation.Reference;
import com.stylefeng.guns.rest.bus.IBusService;
import com.stylefeng.guns.rest.bus.dto.*;
import com.stylefeng.guns.rest.common.RedisUtils;
import com.stylefeng.guns.rest.common.ResponseData;
import com.stylefeng.guns.rest.common.ResponseUtil;
import com.stylefeng.guns.core.constants.RedisConstants;
import com.stylefeng.guns.rest.modular.form.CountPageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(value = "班车服务", description = "班车服务相关接口")
@RestController
@RequestMapping("/bus/")
public class BusController {

    @Reference
    private IBusService busService;
    @Autowired
    private RedisUtils redisUtils;


    @ApiOperation(value = "获取车次列表", notes = "获取车次列表", response = PageCountResponse.class)
    @GetMapping("getCount")
    public ResponseData getCount(CountPageInfo pageInfo) {
        // 本来想用本地缓存的，试试redis吧
        Object obj = redisUtils.get(RedisConstants.COUNTS_EXPIRE.getKey());
        if (obj != null) {
            log.warn("getCount->redis:" + obj.toString());
            return new ResponseUtil().setData(obj);
        }
        PageCountRequest request = new PageCountRequest();
        request.setCurrentPage(pageInfo.getCurrentPage());
        request.setPageSize(pageInfo.getPageSize());
        request.setBusStatus(pageInfo.getBusStatus());
        PageCountResponse response = busService.getCount(request);
        redisUtils.set(RedisConstants.COUNTS_EXPIRE.getKey(), response, RedisConstants.COUNTS_EXPIRE.getTime());
        log.warn("getCount:" + response.toString());
        return new ResponseUtil().setData(response);
    }

    @ApiOperation(value = "获取车次详情", notes = "获取车次详情", response = CountDetailResponse.class)
    @ApiImplicitParam(name = "countId", value = "场次id,CountSimpleDto中的uuid",required = true, dataType = "String", paramType = "query")
    @GetMapping("getCountDetail")
    public ResponseData getCountDetailById(String countId) {
        // id 从本队缓存中取
        Object obj = redisUtils.get(RedisConstants.COUNT_DETAIL_EXPIRE.getKey()+countId);
        if (obj != null) {
            log.warn("getCountDetailById->redis:" + obj.toString());
            return new ResponseUtil().setData(obj);
        }
        CountDetailRequest request = new CountDetailRequest();
        request.setCountId(Integer.parseInt(countId));
        CountDetailResponse response = busService.getCountDetailById(request);
        redisUtils.set(RedisConstants.COUNT_DETAIL_EXPIRE.getKey()+countId, response, RedisConstants.COUNT_DETAIL_EXPIRE.getTime());
        log.warn("getCountDetailById:" + response.toString());
        return new ResponseUtil().setData(response);
    }
}
