package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    /**
     * 新增菜品和口味
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        //向菜品表插入一个数据
        dishMapper.insert(dish);
        //获取生成的id
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size()>0){
            flavors.forEach(dishFlavor->{
                dishFlavor.setDishId(dishId);
            });
            //向口味表插入多条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     *菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //1.判断当前菜品是否在售-->在售不能删除
        for (Long id : ids) {
           Dish dish = dishMapper.getByID(id);
           if(dish.getStatus()== StatusConstant.ENABLE){
               //抛出异常
               throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
           }
        }
        //2.如果被套餐关联则不能删除
        List<Long> setmealIds = setmealDishMapper.getSetmealByDishIds(ids);
        if(setmealIds != null&& setmealIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //3.删除菜品表数据和口味数据
        /*for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除口味表
            dishFlavorMapper.deleteByDishId(id);
        }*/
        //批量删除菜品和口味数据
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询菜品和口味
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品
        Dish dish=dishMapper.getByID(id);
        //根据菜品id查询口味
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        //封装属性到VO对象当中
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }
    /**
     * 修改菜品和口味
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish= new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品表基本信息
        dishMapper.update(dish);
        //删除口味表信息
        dishFlavorMapper.deleteByDishId(dish.getId());
        //插入新的口味信息
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size()>0){
            flavors.forEach(dishFlavor->{
                dishFlavor.setDishId(dishDTO.getId());
            });

            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
       Dish dish =Dish.builder().
               categoryId(categoryId)
               .status(StatusConstant.ENABLE)
               .build();
       return dishMapper.list(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        dishMapper.updateStatus(status,id);
    }
}
