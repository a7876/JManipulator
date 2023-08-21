package top.zproto.jmanipulator.test;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import top.zproto.jmanipulator.core.MethodEntryPoint;

public class Enhancer {


    @Marker
    private final Logger logger = LoggerFactory.getLogger(Enhancer.class);
    public Object template(MethodEntryPoint point) {
        logger.warn("method enter");
        Object res = point.defaultBehaviour();
        logger.warn("method out");
        return res;
    }
}
