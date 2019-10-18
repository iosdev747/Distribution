package container;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Torrent implements Serializable {
    String fileHash;
    String fileName;
    String fileType;
    String[] tags;
    String[] insideFileTypes;
    String author;
    String createdOn;
    long size;
    int numberOfChunks;
    String[] chunkHashes;
    OwlLink[] owlLinks;

    public Torrent(String fileHash, String fileName, String fileType, String[] tags, String[] insideFileTypes, String author, String createdOn, long size, int numberOfChunks, String[] chunkHashes, OwlLink[] owlLinks) {
        this.fileHash = fileHash;
        this.fileName = fileName;
        this.fileType = fileType;
        this.tags = tags;
        this.insideFileTypes = insideFileTypes;
        this.author = author;
        this.createdOn = createdOn;
        this.size = size;
        this.numberOfChunks = numberOfChunks;
        this.chunkHashes = chunkHashes;
        this.owlLinks = owlLinks;
    }

    public Torrent() {
    }

    @Override
    public String toString() {
        //        for (String chunk : this.chunkHashes) {
//            output += "- " + chunk + "\n";
//        }
        return "File Name: " + this.fileName + "   Hash: " + this.fileHash + "   Size in bytes: " + this.size;
    }

}
