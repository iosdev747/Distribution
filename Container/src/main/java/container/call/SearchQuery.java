package container.call;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchQuery {
    boolean[] booleans;
    String searchText;
    String fileTypeText;
    String[] insideFileTypes;
    String[] tags;
    int size;
    String fileHash;

    public SearchQuery() {
    }

    public SearchQuery(boolean[] booleans, String searchText, String fileTypeText, String[] insideFileTypes, String[] tags, int size, String fileHash) {
        this.booleans = booleans;
        this.searchText = searchText;
        this.fileTypeText = fileTypeText;
        this.insideFileTypes = insideFileTypes;
        this.tags = tags;
        this.size = size;
        this.fileHash = fileHash;
    }
}
