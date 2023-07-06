package com.example.cache;

import com.example.components.Cacheable;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.example.utils.CommonUtil.*;
import static java.util.stream.Collectors.joining;

//All Operations are having Time Complexity: O(1)
public abstract class CacheRefreshStrategy<K, C extends Cacheable> {

    @Autowired
    public RestHighLevelClient client;

    @Autowired
    public RedisTemplate redisTemplate;

    public static final Logger LOGGER = LoggerFactory.getLogger(CacheRefreshStrategy.class);

    public abstract String cacheIdentifierField();

    public abstract C getExistingObjectByIdentifier(Object id);

    //Time Complexity: O(1)
    public static final BiFunction<Object, List<String>, Object> EXTRACT_CACHE_KEY = (cacheObject, cacheKeyFields) -> {
        Object keyForExistingCache = null;
        Collection<Method> gettersForCacheKey = cacheKeyFields.stream().map(cacheKeyField -> fieldToGetterExtractor.apply(cacheObject.getClass(), Sets.newHashSet(cacheKeyField)).get(extractField.apply(cacheObject.getClass(), cacheKeyField))).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(cacheKeyFields)) {
            if (cacheKeyFields.size() > 1) {
                keyForExistingCache = gettersForCacheKey.stream().map(getter -> get.apply(cacheObject, getter.getName())).map(String::valueOf).collect(joining("-"));
            } else{
                keyForExistingCache = gettersForCacheKey.stream().map(getter -> get.apply(cacheObject, getter.getName())).findFirst().get();
            }
        }
        return keyForExistingCache;
    };

    //Time Complexity: O(1)

    public abstract Map<String, Object> convertObjectToMap(C object);
    public abstract C convertMapToObject(Map<String, Object> map);

    public final C refreshCache(C newerObject, String cacheName, String isDelete){
        K keyForNewerCache = (K) newerObject.getId();
        String id = String.valueOf(keyForNewerCache);
        C originalObject = null;
        try{
            GetResponse getResponse = client.get(new GetRequest(cacheName, id), RequestOptions.DEFAULT);
            if(Objects.isNull(getResponse) || !getResponse.isExists()){
                LOGGER.info("Document with id {} does not exist", id);
            } else {
                if(getResponse.isSourceEmpty()){
                    LOGGER.info("Document with id {} does not have any source", id);
                } else{
                    Map<String, Object> originalSource = getResponse.getSource();
                    originalObject = convertMapToObject(originalSource);
                    LOGGER.info("Document with id {} has source {}", id, originalSource);
                    LOGGER.info("Value of Original Object from Elastic Search {}", originalObject);
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        originalObject = (C) redisTemplate.opsForValue().get(keyForNewerCache);
        if(!Objects.isNull(originalObject)){
            LOGGER.info("Value of Original Object from Redis {}", originalObject);
        }
        if (StringUtils.isBlank(isDelete)) {
            isDelete = "N";
        }
        if(StringUtils.equalsIgnoreCase("N", isDelete)){
            try {
                IndexRequest indexRequest = new IndexRequest(cacheName);
                indexRequest.id(id);
                indexRequest.source(convertObjectToMap(newerObject));
                IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

            } catch (IOException e) {
                e.printStackTrace();
            }
            try{
                redisTemplate.opsForValue().set(keyForNewerCache, newerObject);
            }catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                DeleteRequest deleteRequest = new DeleteRequest(cacheName, id);
                client.delete(deleteRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try{
                redisTemplate.delete(keyForNewerCache);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return originalObject;
    }

}


