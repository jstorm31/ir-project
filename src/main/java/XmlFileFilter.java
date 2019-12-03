// Source: https://www.tutorialspoint.com/lucene/lucene_first_application.htm

import java.io.File;
import java.io.FileFilter;

public class XmlFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase().endsWith(".xml");
    }
}
