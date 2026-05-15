package org.dromara.common.mybatis.core.mapper;

import com.baomidou.mybatisplus.core.conditions.AbstractLambdaWrapper;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.dromara.common.mybatis.core.query.LambdaQueryCondition;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Mapper ? Lambda CRUD ??????
 *
 * @param <T> table ??
 * @param <V> vo ??
 * @author Lion Li
 */
public class LambdaCrudChainWrapper<T, V> extends AbstractLambdaWrapper<T, LambdaCrudChainWrapper<T, V>>
    implements Query<LambdaCrudChainWrapper<T, V>, T, SFunction<T, ?>>,
    Update<LambdaCrudChainWrapper<T, V>, SFunction<T, ?>>,
    LambdaQueryCondition<T, LambdaCrudChainWrapper<T, V>> {

    private final BaseMapperPlus<T, V> crudMapper;
    private final List<String> sqlSet;
    private SharedString sqlSelect = new SharedString();

    public LambdaCrudChainWrapper(BaseMapperPlus<T, V> crudMapper) {
        this.crudMapper = crudMapper;
        super.setEntityClass(crudMapper.currentModelClass());
        super.initNeed();
        this.sqlSet = new ArrayList<>();
    }

    LambdaCrudChainWrapper(BaseMapperPlus<T, V> crudMapper, T entity, Class<T> entityClass, SharedString sqlSelect,
                           List<String> sqlSet, AtomicInteger paramNameSeq, Map<String, Object> paramNameValuePairs,
                           MergeSegments mergeSegments, SharedString paramAlias, SharedString lastSql,
                           SharedString sqlComment, SharedString sqlFirst) {
        this.crudMapper = crudMapper;
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.sqlSelect = sqlSelect == null ? new SharedString() : sqlSelect;
        this.sqlSet = sqlSet == null ? new ArrayList<>() : sqlSet;
        this.paramNameSeq = paramNameSeq;
        this.paramNameValuePairs = paramNameValuePairs;
        this.expression = mergeSegments;
        this.paramAlias = paramAlias;
        this.lastSql = lastSql;
        this.sqlComment = sqlComment;
        this.sqlFirst = sqlFirst;
    }

    @Override
    public LambdaCrudChainWrapper<T, V> select(boolean condition, List<SFunction<T, ?>> columns) {
        if (condition && CollectionUtils.isNotEmpty(columns)) {
            this.sqlSelect.setStringValue(columnsToString(false, columns));
        }
        return typedThis;
    }

    @SafeVarargs
    public final LambdaCrudChainWrapper<T, V> select(SFunction<T, ?>... columns) {
        return select(true, CollectionUtils.toList(columns));
    }

    @SafeVarargs
    public final LambdaCrudChainWrapper<T, V> select(boolean condition, SFunction<T, ?>... columns) {
        return select(condition, CollectionUtils.toList(columns));
    }

    @Override
    public LambdaCrudChainWrapper<T, V> select(Class<T> entityClass, Predicate<TableFieldInfo> predicate) {
        if (entityClass == null) {
            entityClass = getEntityClass();
        } else {
            setEntityClass(entityClass);
        }
        Assert.notNull(entityClass, "entityClass can not be null");
        this.sqlSelect.setStringValue(TableInfoHelper.getTableInfo(entityClass).chooseSelect(predicate));
        return typedThis;
    }

    @Override
    public String getSqlSelect() {
        return sqlSelect.getStringValue();
    }

    @Override
    public LambdaCrudChainWrapper<T, V> set(boolean condition, SFunction<T, ?> column, Object val, String mapping) {
        return maybeDo(condition, () -> {
            String sql = formatParam(mapping, val);
            sqlSet.add(columnToString(column) + Constants.EQUALS + sql);
        });
    }

    /**
     * ??? null ????????
     *
     * @param column ??
     * @param value  ?
     * @return this
     */
    public LambdaCrudChainWrapper<T, V> setIfPresent(SFunction<T, ?> column, Object value) {
        return set(value != null, column, value);
    }

    /**
     * ?????????????
     *
     * @param column ??
     * @param value  ?
     * @return this
     */
    public LambdaCrudChainWrapper<T, V> setIfText(SFunction<T, ?> column, String value) {
        return set(org.dromara.common.core.utils.StringUtils.isNotBlank(value), column, value);
    }

    @Override
    public LambdaCrudChainWrapper<T, V> setSql(boolean condition, String setSql, Object... params) {
        return maybeDo(condition && StringUtils.isNotBlank(setSql), () -> sqlSet.add(formatSqlMaybeWithParam(setSql, params)));
    }

    @Override
    public LambdaCrudChainWrapper<T, V> setIncrBy(boolean condition, SFunction<T, ?> column, Number val) {
        return maybeDo(condition, () -> {
            String realColumn = columnToString(column);
            String realVal = val instanceof BigDecimal ? ((BigDecimal) val).toPlainString() : String.valueOf(val);
            sqlSet.add(String.format("%s=%s + %s", realColumn, realColumn, realVal));
        });
    }

    @Override
    public LambdaCrudChainWrapper<T, V> setDecrBy(boolean condition, SFunction<T, ?> column, Number val) {
        return maybeDo(condition, () -> {
            String realColumn = columnToString(column);
            String realVal = val instanceof BigDecimal ? ((BigDecimal) val).toPlainString() : String.valueOf(val);
            sqlSet.add(String.format("%s=%s - %s", realColumn, realColumn, realVal));
        });
    }

    @Override
    public String getSqlSet() {
        if (CollectionUtils.isEmpty(sqlSet)) {
            return null;
        }
        return String.join(Constants.COMMA, sqlSet);
    }

    /**
     * ?????? Wrapper?
     *
     * @return this
     */
    public LambdaCrudChainWrapper<T, V> getWrapper() {
        return typedThis;
    }

    public LambdaCrudChainWrapper<T, V> findInSet(Object value, SFunction<T, ?> column) {
        return findInSet(true, value, column);
    }

    public LambdaCrudChainWrapper<T, V> findInSet(boolean condition, Object value, SFunction<T, ?> column) {
        return findInSet(condition, value, columnToString(column));
    }

    public LambdaCrudChainWrapper<T, V> findInSetIfPresent(Object value, SFunction<T, ?> column) {
        return findInSet(value != null, value, column);
    }

    /**
     * ???? Wrapper?
     *
     * @return this
     */
    public LambdaCrudChainWrapper<T, V> build() {
        return typedThis;
    }

    /**
     * ???????
     *
     * @return ????
     */
    public List<T> list() {
        return crudMapper.selectList(typedThis);
    }

    /**
     * ?????????
     *
     * @param page ????
     * @return ??????
     */
    public List<T> list(IPage<T> page) {
        return crudMapper.selectList(page, typedThis);
    }

    /**
     * ?? VO ???
     *
     * @return VO ??
     */
    public List<V> voList() {
        return crudMapper.selectVoList(typedThis);
    }

    /**
     * ?????????
     *
     * @return ??????
     */
    public List<Object> objs() {
        return crudMapper.selectObjs(typedThis);
    }

    /**
     * ??????????????
     *
     * @param mapper ????
     * @param <C>    ??????
     * @return ??????
     */
    public <C> List<C> objs(Function<? super Object, C> mapper) {
        return crudMapper.selectObjs(typedThis, mapper);
    }

    /**
     * ???????
     *
     * @return ??
     */
    public T one() {
        return crudMapper.selectOne(typedThis);
    }

    /**
     * ???????
     *
     * @param throwEx ???????????
     * @return ??
     */
    public T one(boolean throwEx) {
        return crudMapper.selectOne(typedThis, throwEx);
    }

    /**
     * ?????? Optional?
     *
     * @return Optional ??
     */
    public Optional<T> oneOpt() {
        return Optional.ofNullable(one());
    }

    /**
     * ???? VO?
     *
     * @return VO
     */
    public V voOne() {
        return crudMapper.selectVoOne(typedThis);
    }

    /**
     * ???? VO?
     *
     * @param throwEx ???????????
     * @return VO
     */
    public V voOne(boolean throwEx) {
        return crudMapper.selectVoOne(typedThis, throwEx);
    }

    /**
     * ?????
     *
     * @return ??
     */
    public Long count() {
        return crudMapper.selectCount(typedThis);
    }

    /**
     * ???????
     *
     * @return ????
     */
    public boolean exists() {
        return crudMapper.exists(typedThis);
    }

    /**
     * ???????
     *
     * @param page ????
     * @param <P>  ????
     * @return ????
     */
    public <P extends IPage<T>> P page(P page) {
        return crudMapper.selectPage(page, typedThis);
    }

    /**
     * ?? VO ???
     *
     * @param page ????
     * @param <P>  ????
     * @return VO ??
     */
    public <P extends IPage<V>> P voPage(IPage<T> page) {
        return crudMapper.selectVoPage(page, typedThis);
    }

    /**
     * ?????
     *
     * @return ??????
     */
    public boolean delete() {
        return deleteCount() > 0;
    }

    /**
     * ?????
     *
     * @return ????
     */
    public int deleteCount() {
        return crudMapper.delete(typedThis);
    }

    /**
     * ?? set ???????
     *
     * @return ??????
     */
    public boolean update() {
        return updateCount() > 0;
    }

    /**
     * ??????????????
     *
     * @param entity ??
     * @return ??????
     */
    public boolean update(T entity) {
        return updateCount(entity) > 0;
    }

    /**
     * ?? set ???????
     *
     * @return ????
     */
    public int updateCount() {
        return crudMapper.update(typedThis);
    }

    /**
     * ??????????????
     *
     * @param entity ??
     * @return ????
     */
    public int updateCount(T entity) {
        return crudMapper.update(entity, typedThis);
    }

    @Override
    protected LambdaCrudChainWrapper<T, V> instance() {
        return new LambdaCrudChainWrapper<>(crudMapper, getEntity(), getEntityClass(), null, null, paramNameSeq,
            paramNameValuePairs, new MergeSegments(), paramAlias, SharedString.emptyString(), SharedString.emptyString(),
            SharedString.emptyString());
    }

    @Override
    public void clear() {
        super.clear();
        sqlSelect.toNull();
        sqlSet.clear();
    }

}
