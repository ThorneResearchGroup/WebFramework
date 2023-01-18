package tech.tresearchgroup.palila.controller.search;

import java.util.List;

public interface SearchFunctionality {
    List search(String query);

    void createDocument(Object object);

    void updateDocument(Object object);

    void deleteDocument(long id);

    void deleteAllDocuments();

    void reindex(int maxResultsSize);
}
