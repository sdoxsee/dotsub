package com.mycompany.myapp.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.mycompany.myapp.domain.Item;

import com.mycompany.myapp.repository.ItemRepository;
import com.mycompany.myapp.web.rest.util.HeaderUtil;
import com.mycompany.myapp.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Item.
 */
@RestController
@RequestMapping("/api")
public class ItemResource {

    private final Logger log = LoggerFactory.getLogger(ItemResource.class);

    @Inject
    private ItemRepository itemRepository;

    /**
     * POST  /items : Create a new item.
     *
     * @param item the item to create
     * @return the ResponseEntity with status 201 (Created) and with body the new item, or with status 400 (Bad Request) if the item has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/items")
    @Timed
    public ResponseEntity<Item> createItem(@RequestBody Item item) throws URISyntaxException {
        log.debug("REST request to save Item : {}", item);
        if (item.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("item", "idexists", "A new item cannot already have an ID")).body(null);
        }
        if (item.getCreationDate() == null) {
            item.creationDate(ZonedDateTime.now());
        }
        Item result = itemRepository.save(item);
        return ResponseEntity.created(new URI("/api/items/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("item", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /items : Updates an existing item.
     *
     * @param item the item to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated item,
     * or with status 400 (Bad Request) if the item is not valid,
     * or with status 500 (Internal Server Error) if the item couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/items")
    @Timed
    public ResponseEntity<Item> updateItem(@RequestBody Item item) throws URISyntaxException {
        log.debug("REST request to update Item : {}", item);
        if (item.getId() == null) {
            return createItem(item);
        }
        Item result = itemRepository.save(item);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("item", item.getId().toString()))
            .body(result);
    }

    /**
     * GET  /items : get all the items.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of items in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping("/items")
    @Timed
    public ResponseEntity<List<Item>> getAllItems(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Items");
        Page<Item> page = itemRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/items");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /items/:id : get the "id" item.
     *
     * @param id the id of the item to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the item, or with status 404 (Not Found)
     */
    @GetMapping("/items/{id}")
    @Timed
    public ResponseEntity<Item> getItem(@PathVariable Long id) {
        log.debug("REST request to get Item : {}", id);
        Item item = itemRepository.findOne(id);
        return Optional.ofNullable(item)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /items/:id : delete the "id" item.
     *
     * @param id the id of the item to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/items/{id}")
    @Timed
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        log.debug("REST request to delete Item : {}", id);
        itemRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("item", id.toString())).build();
    }

}
