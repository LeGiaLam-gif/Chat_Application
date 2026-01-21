package client.service;

import client.core.ChatClient;
import common.protocol.Message;
import common.protocol.MessageType;
import common.util.ChecksumUtil;
import java.io.*;
import java.util.Arrays;

public class FileSender {
    private static final int CHUNK_SIZE = 64 * 1024;
    private final ChatClient client;
    
    public FileSender(ChatClient client) {
        this.client = client;
    }
    
    public void sendFile(File file, String receiver) throws Exception {
        String checksum = ChecksumUtil.calculateMD5(file);
        
        // Send metadata
        Message meta = new Message(MessageType.FILE_META, 
            client.getUsername(), receiver, "");
        meta.setFileMetadata(file.getName(), file.length(), checksum);
        client.send(meta);
        
        // Send chunks
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int sequence = 0;
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) > 0) {
                byte[] chunk = Arrays.copyOf(buffer, bytesRead);
                
                Message chunkMsg = new Message(MessageType.FILE_CHUNK,
                    client.getUsername(), receiver, "");
                chunkMsg.setChunkData(sequence, chunk);
                client.send(chunkMsg);
                
                sequence++;
            }
        }
    }
}
