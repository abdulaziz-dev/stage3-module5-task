package com.mjc.school.repository.impl;

import com.mjc.school.repository.NewsRepository;
import com.mjc.school.repository.model.AuthorModel;
import com.mjc.school.repository.model.NewsModel;
import com.mjc.school.repository.model.TagModel;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NewsRepositoryImpl extends AbstractRepository<NewsModel, Long> implements NewsRepository {


    @Override
    protected void setFields(NewsModel oldModel, NewsModel newModel) {
        oldModel.setTitle(newModel.getTitle());
        oldModel.setContent(newModel.getContent());
        oldModel.setAuthor(newModel.getAuthor());
        oldModel.setTags(newModel.getTags());
    }


    public List<NewsModel> readByParams(Long tagId, String tagName, String authorName, String title, String content){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<NewsModel> cr = cb.createQuery(NewsModel.class);
        Root<NewsModel> root = cr.from(NewsModel.class);

        boolean validTagId = tagId != null;
        boolean validTagName = tagName != null && !tagName.isBlank();
        boolean validAuthor = authorName != null && !authorName.isBlank();

        List<Predicate> predicates = new ArrayList<>();

        if (validTagId || validTagName){
            Join<NewsModel, TagModel> tags = root.join("tags", JoinType.LEFT);
            if (validTagId){
                Predicate tagIdCheck = cb.equal(tags.get("id"), tagId);
                predicates.add(tagIdCheck);
            }
            if (validTagName){
                Predicate tagNameCheck = cb.equal(tags.get("name"), tagName);
                predicates.add(tagNameCheck);
            }
        }

        if (validAuthor){
            Join<NewsModel, AuthorModel> author = root.join("author", JoinType.LEFT);
            Predicate authorCheck = cb.equal(author.get("name"), authorName);
            predicates.add(authorCheck);
        }

        if (title != null && !title.isBlank()){
            Predicate titleCheck  = cb.like(root.get("title"), '%' + title + '%');
            predicates.add(titleCheck);
        }
        if (content != null && !content.isBlank()){
            Predicate contentCheck  = cb.like(root.get("content"), '%' + content + '%');
            predicates.add(contentCheck);
        }

        cr.select(root).distinct(true).where(predicates.toArray(new Predicate[0]));
        TypedQuery<NewsModel> query = entityManager.createQuery(cr);
        return query.getResultList();
    }

}
