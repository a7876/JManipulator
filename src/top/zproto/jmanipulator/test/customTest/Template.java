package top.zproto.jmanipulator.test.customTest;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import top.zproto.jmanipulator.core.MethodEntryPoint;

public class Template {
    @Marker
    private final Logger logger = LoggerFactory.getLogger(Template.class);
    public Object template(MethodEntryPoint point) {
        logger.warn("method enter");
        Object res = point.defaultBehaviour();
        logger.warn("method out");
        return res;
    }
}
