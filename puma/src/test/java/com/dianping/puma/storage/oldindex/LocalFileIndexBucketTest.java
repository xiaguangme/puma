package com.dianping.puma.storage.oldindex;

import com.dianping.puma.storage.Sequence;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

/**
 * @author damonzhu
 */
public class LocalFileIndexBucketTest {

    private File file;

    private String name;

    private IndexItemConverter<IndexValueImpl> valueConvertor = new IndexValueConverter();

    @Before
    public void setup() throws IOException {
        File path = new File(System.getProperty("java.io.tmpdir", "."), "puma");
        if (!path.exists()) {
            path.mkdir();
        }

        this.name = "20150713-Bucket-15.l2idx";

        this.file = new File(System.getProperty("java.io.tmpdir", "."), "puma/20150713-Bucket-15.l2idx");
        this.file.createNewFile();

        DataOutputStream output = new DataOutputStream(new FileOutputStream(this.file));

        for (int i = 0; i < 1000; i++) {
            IndexValueImpl l2Index = new IndexValueImpl();
            l2Index.setIndexKey(new IndexKeyImpl(1 + i, 1L, "mysql-binlog.0000001", 4L + i));
            l2Index.setDdl(false);
            l2Index.setDml(true);
            l2Index.setSequence(new Sequence(123123L + i, 1));
            byte[] convertToObj = (byte[]) valueConvertor.convertToObj(l2Index);

            output.writeInt(convertToObj.length);
            output.write(convertToObj);
        }

        output.close();
    }

    @After
    public void cleanUp() {
        if (this.file.exists()) {
            this.file.delete();
        }
    }

    @Test
    public void testGetNext() throws IOException {
        LocalFileIndexBucket<IndexKeyImpl, IndexValueImpl> indexBucket = new LocalFileIndexBucket<IndexKeyImpl, IndexValueImpl>(
                this.name, this.file, valueConvertor);
        indexBucket.start();

        int i = 0;
        try {
            for (; i < 1001; i++) {
                IndexValueImpl next = indexBucket.next();

                Assert.assertEquals("mysql-binlog.0000001", next.getIndexKey().getBinlogFile());
                Assert.assertEquals(4L + i, next.getIndexKey().getBinlogPosition());
                Assert.assertEquals(1L, next.getIndexKey().getServerId());
                Assert.assertEquals(i + 1, next.getIndexKey().getTimestamp());
                Assert.assertEquals(false, next.isDdl());
                Assert.assertEquals(true, next.isDml());
                Assert.assertEquals(123123L + i, next.getSequence().longValue());
                Assert.assertEquals(1, next.getSequence().getLen());
            }
        } catch (EOFException eof) {
            Assert.assertEquals(i, 1000);
        }
    }

    @Test
    public void testLocateExclusive() throws IOException {
        IndexKeyImpl searchKey = new IndexKeyImpl(1 + 10, 1L, "mysql-binlog.0000001", 4L + 10);

        LocalFileIndexBucket<IndexKeyImpl, IndexValueImpl> indexBucket = new LocalFileIndexBucket<IndexKeyImpl, IndexValueImpl>(
                this.name, this.file, valueConvertor, searchKey, false);
        indexBucket.start();

        for (int i = 11; i < 1000; i++) {
            IndexValueImpl next = indexBucket.next();

            Assert.assertEquals("mysql-binlog.0000001", next.getIndexKey().getBinlogFile());
            Assert.assertEquals(4L + i, next.getIndexKey().getBinlogPosition());
            Assert.assertEquals(1L, next.getIndexKey().getServerId());
            Assert.assertEquals(false, next.isDdl());
            Assert.assertEquals(true, next.isDml());
            Assert.assertEquals(123123L + i, next.getSequence().longValue());
        }
    }

    @Test
    public void testLocateInclusive() throws IOException {
        IndexKeyImpl searchKey = new IndexKeyImpl(1 + 10, 1L, "mysql-binlog.0000001", 4L + 10);

        LocalFileIndexBucket<IndexKeyImpl, IndexValueImpl> indexBucket = new LocalFileIndexBucket<IndexKeyImpl, IndexValueImpl>(
                this.name, this.file, valueConvertor, searchKey, true);
        indexBucket.start();

        for (int i = 10; i < 1000; i++) {
            IndexValueImpl next = indexBucket.next();

            Assert.assertEquals("mysql-binlog.0000001", next.getIndexKey().getBinlogFile());
            Assert.assertEquals(4L + i, next.getIndexKey().getBinlogPosition());
            Assert.assertEquals(1L, next.getIndexKey().getServerId());
            Assert.assertEquals(false, next.isDdl());
            Assert.assertEquals(true, next.isDml());
            Assert.assertEquals(123123L + i, next.getSequence().longValue());
        }
    }
}