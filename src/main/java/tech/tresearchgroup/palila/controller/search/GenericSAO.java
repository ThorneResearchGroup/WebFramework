package tech.tresearchgroup.palila.controller.search;

import com.google.gson.Gson;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.Task;
import com.meilisearch.sdk.model.SearchResult;
import tech.tresearchgroup.palila.controller.database.GenericDAO;
import tech.tresearchgroup.palila.model.BaseSettings;
import tech.tresearchgroup.palila.model.enums.SearchMethodEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericSAO implements GenericSearchFunctionality {
    private final Gson gson;

    public GenericSAO(Gson gson) {
        this.gson = gson;
    }

    @Override
    public List search(String query, Index index) throws Exception {
        SearchRequest searchRequest = new SearchRequest().setQ(query);
        SearchResult queryData = index.search(searchRequest);
        Object[] data = queryData.getHits().toArray();
        return new ArrayList(Arrays.asList(data));
    }

    @Override
    public void createDocument(Object object, Index index) throws Exception {
        if (BaseSettings.searchMethod.equals(SearchMethodEnum.SEARCH_SERVER)) {
            index.addDocuments(gson.toJson(object));
        }
    }

    @Override
    public void updateDocument(Object object, Index index) throws Exception {
        if (BaseSettings.searchMethod.equals(SearchMethodEnum.SEARCH_SERVER)) {
            index.updateDocuments(gson.toJson(object));
        }
    }

    @Override
    public void deleteDocument(long id, Index index) throws Exception {
        if (BaseSettings.searchMethod.equals(SearchMethodEnum.SEARCH_SERVER)) {
            index.deleteDocument(String.valueOf(id));
        }
    }

    @Override
    public void deleteAllDocuments(Index index) throws Exception {
        if (BaseSettings.searchMethod.equals(SearchMethodEnum.SEARCH_SERVER)) {
            Task task = index.deleteAllDocuments();
            task.wait();
        }
    }

    @Override
    public void reindex(int maxResultsSize, GenericDAO genericDAO, Index index, Class theClass) throws Exception {
        if (BaseSettings.searchMethod.equals(SearchMethodEnum.SEARCH_SERVER)) {
            long totalPages = genericDAO.getTotalPages(maxResultsSize, theClass);
            for (int i = 0; i <= totalPages; i++) {
                List items = genericDAO.readPaginated(maxResultsSize, i, theClass, false);
                for (Object item : items) {
                    String json = gson.toJson(item);
                    index.addDocuments(json);
                }
            }
        }
    }
}
