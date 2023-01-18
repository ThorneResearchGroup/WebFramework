package tech.tresearchgroup.palila.controller.search;

import com.meilisearch.sdk.Index;
import tech.tresearchgroup.palila.controller.database.GenericDAO;

import java.util.List;

public interface GenericSearchFunctionality {
    List search(String query, Index index) throws Exception;

    void createDocument(Object object, Index index) throws Exception;

    void updateDocument(Object object, Index index) throws Exception;

    void deleteDocument(long id, Index index) throws Exception;

    void deleteAllDocuments(Index index) throws Exception;

    void reindex(int maxResultsSize, GenericDAO genericDAO, Index index, Class theClass) throws Exception;
}
