package ir.sahab.nimbo.jimbo.hbase;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static ir.sahab.nimbo.jimbo.elastic.Config.*;

public class HBase {

    private static final Logger logger = LoggerFactory.getLogger(HBase.class);

    private static HBase hbase = new HBase();
    private TableName tableName;
    Table table = null;

    private HBase() {
        tableName = TableName.valueOf(HBASE_TABLE_NAME);
        Configuration config = HBaseConfiguration.create();
        String path = Objects.requireNonNull(this.getClass().getClassLoader().getResource(HBASE_SITE_DIR)).getPath();
        config.addResource(new Path(path));
        path = Objects.requireNonNull(this.getClass().getClassLoader().getResource(HBASE_CORE_DIR)).getPath();
        config.addResource(new Path(path));
        boolean conn = true;
        Connection connection = null;
        while (conn) {
            try {
                connection = ConnectionFactory.createConnection(config);
                conn = false;
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        try {
            Admin admin = connection.getAdmin();
            if (!admin.tableExists(tableName)) {
                initialize(admin);
            }
            table = connection.getTable(tableName);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static HBase getInstance() {
        return hbase;
    }

    private static String getHash(String inp) {
        return DigestUtils.md5Hex(inp);
    }

    public int getNumberOfReferences(String sourceUrl) {
        Get get = new Get(makeRowKey(sourceUrl).getBytes());
        get.addColumn(HBASE_MARK_CF_NAME.getBytes(), HBASE_MARK_Q_NAME_NUMBER_OF_REFERENCES.getBytes());
        Result result;
        try {
            result = table.get(get);
            if (result != null) {
                byte[] res = result.getValue(HBASE_MARK_CF_NAME.getBytes(),
                        HBASE_MARK_Q_NAME_NUMBER_OF_REFERENCES.getBytes());
                if(res != null)
                    return Bytes.toInt(res);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return 0;
    }

    private void initialize(Admin admin) {
        try {
            HTableDescriptor desc = new HTableDescriptor(tableName);
            desc.addFamily(new HColumnDescriptor(HBASE_DATA_CF_NAME));
            desc.addFamily(new HColumnDescriptor(HBASE_MARK_CF_NAME));
            //TODO region bandy
            admin.createTable(desc);
//            TableDescriptorBuilder tableDescriptorBuilder =
//                    TableDescriptorBuilder.newBuilder(tableName);
//            List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
//            columnFamilyDescriptors.add(ColumnFamilyDescriptorBuilder.newBuilder(cFAnchor.getBytes()).build());
//            columnFamilyDescriptors.add(ColumnFamilyDescriptorBuilder.newBuilder(cFMeta.getBytes()).build());
//            columnFamilyDescriptors.add(ColumnFamilyDescriptorBuilder.newBuilder(cFTitle.getBytes()).build());
//            columnFamilyDescriptors.add(ColumnFamilyDescriptorBuilder.newBuilder(cFText.getBytes()).build());
//            //columnFamilyDescriptorBuilder.setValue("col1".getBytes(), "val1".getBytes());
//            //columnFamilyDescriptorBuilder2.setValue("col2".getBytes(), "val2".getBytes());
//            tableDescriptorBuilder.setColumnFamilies(columnFamilyDescriptors);
//            admin.createTable(tableDescriptorBuilder.build());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    String makeRowKey(String row) {
        return getHash(row);
    }
}
