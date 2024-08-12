package com.example.redis;

import com.example.redis.domain.Item;
import com.example.redis.domain.ItemDto;
import com.example.redis.repo.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
public class ItemService {
    /*  @Cacheable: 시에 해당 키가 있는 경우 메서드를 실행하지 않고, 캐시된 값을 반환.
        캐시에 키가 없을 경우에만 메서드를 실행하고, 결과를 캐시에 저장 (Cache-Aside)

         @CachePut: 항상 메서드를 실행한 후, 그 결과를 캐시에 저장. 캐시에 저장된 값을 업데이트. (Write-Through)
     */

    private final ItemRepository itemRepository;
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    // #result.id: create()를 통하여 반환하는 타입(ItemDto)의 id 값
    @CachePut(cacheNames = "itemCache", key = "#result.id")
    public ItemDto create(ItemDto dto) {
        return ItemDto.fromEntity(itemRepository.save(Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .build()));
    }

    @Cacheable(cacheNames = "itemAllCache", key = "methodName")
    public List<ItemDto> readAll() {
        return itemRepository.findAll()
                .stream()
                .map(ItemDto::fromEntity)
                .toList();
    }

    // cacheNames: 메서드로 인해서 만들어질 캐시를 지칭하는 이름
    // key: 캐시의 데이터(itemCache)를 구분하기 위해 활용하는 값 / ex) 파라미터의 id가 다르면 캐시에 있는 데이터 값도 다르다
    // args[0]: 메서드의 첫 번째 인자
    @Cacheable(cacheNames = "itemCache", key = "args[0]") // 이 메서드의 결과는 캐싱이 가능하다
    public ItemDto readOne(Long id) {
        return itemRepository.findById(id)
                .map(ItemDto::fromEntity)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @CachePut(cacheNames = "itemCache", key = "args[0]")
//  update가 일어나면 readAll에 있던 캐시를 모두 지우고 싶을경우
//  @CacheEvict(cacheNames = "itemAllCache", allEntries = true)
    // 특정 키를 지정해서 캐시 삭제
    @CacheEvict(cacheNames = "itemAllCache", key = "'readAll'")
    public ItemDto update(Long id, ItemDto dto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        return ItemDto.fromEntity(itemRepository.save(item));
    }

    public void delete(Long id) {
        itemRepository.deleteById(id);
    }


    @Cacheable( // 조회만 하기 때문에 @Cacheable 사용
            cacheNames = "itemSearchCache",
            // Postman의 Params에 {key:value}값으로 줄 수 있다/ {q:monitor}, {page:1}, {size:5}
            key = "{ args[0], args[1].pageNumber, args[1].pageSize }"
    )
    public Page<ItemDto> searchByName(String query, Pageable pageable) {
        return itemRepository.findAllByNameContains(query, pageable)
                .map(ItemDto::fromEntity);
    }

}
