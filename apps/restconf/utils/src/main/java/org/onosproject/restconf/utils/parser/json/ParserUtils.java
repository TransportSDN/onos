package org.onosproject.restconf.utils.parser.json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.onosproject.restconf.utils.exceptions.JsonParseException;
import org.onosproject.restconf.utils.parser.api.JsonBuilder;
import org.onosproject.restconf.utils.parser.api.JsonWalker;
import org.onosproject.yms.ydt.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utils to complete the conversion between JSON and YDT(YANG DATA MODEL).
 */
public final class ParserUtils {

    private static final Splitter SLASH_SPLITTER = Splitter.on('/');
    private static final Splitter COMMA_SPLITTER = Splitter.on(',');
    private static final String EQUAL = "=";
    private static final String COMMA = ",";
    private static final String COLON = ":";
    private final static String URI_ENCODING_CHAR_SET = "ISO-8859-1";

    /**
     * Converts  URI identifier to YDT builder.
     *
     * @param identifier the uri identifier from web request.
     * @param builder    the base ydt builder
     * @return the YDT builder with the tree info of identifier
     */
    public static YdtBuilder convertUriToYdt(String identifier, YdtBuilder builder) {
        checkNotNull(identifier, "uri identifier should not be null");
        List<String> paths = urlPathArgsDecode(SLASH_SPLITTER.split(identifier));
        if (paths.isEmpty()) {
            return null;
        }
        processPathSegments(paths, builder);
        return builder;
    }

    /**
     * Converts  JSON objectNode to YDT builder. The objectNode can be any standard JSON node, node
     * just for RESTconf payload.
     *
     * @param objectNode the objectNode from web request.
     * @param builder    the base ydt builder
     * @return the YDT builder with the tree info of identifier
     */
    public static YdtBuilder convertJsonToYdt(ObjectNode objectNode, YdtBuilder builder) {
        JsonWalker walker = new DefaultJsonWalker();
        JsonToYdtListener listener = new JsonToYdtListener(builder);
        walker.walk(listener, null, objectNode);
        return builder;
    }

    /**
     * Converts a Ydt context tree to a JSON object.
     *
     * @param ydtContext a abstract data model for YANG data.
     * @param walker     abstraction of an entity which provides interfaces for YDT walk.
     * @return the JSON node corresponding the YANG data
     */
    public static ObjectNode convertYdtToJson(YdtContext ydtContext, YdtWalker walker) {
        JsonBuilder builder = new DefaultJsonBuilder();
        YdtListener listener = new YdtToJsonListener(builder);
        walker.walk(listener, ydtContext);
        return builder.getTreeNode();
    }

    /**
     * Converts a list of path segments to a YDT builder tree.
     *
     * @param builder the base YDT builder
     * @param paths   the list of path segments split from URI
     * @return the YDT builder with the tree info of paths
     */
    private static YdtBuilder processPathSegments(List<String> paths, YdtBuilder builder) {
        if (paths.isEmpty()) {
            return builder;
        }
        final String firstSeg = paths.iterator().next();
        if (firstSeg.contains(COLON)) {
            addModule(builder, firstSeg);
            addNode(builder, firstSeg);
        } else if (firstSeg.contains(EQUAL)) {
            addListOrLeafList(builder, firstSeg);
        } else {
            return processLeaf(builder, firstSeg);
        }

        if (paths.size() == 1) {
            return builder;
        }
        List<String> remainPaths = paths.subList(1, paths.size());
        processPathSegments(remainPaths, builder);

        return builder;
    }

    private static YdtBuilder addModule(YdtBuilder builder, String path) {
        String moduleName = getPreSegment(path, COLON);
        if (moduleName == null) {
            throw new JsonParseException("Illegal URI, First node should be in format \"moduleName:nodeName\"");
        }
        builder.addChild(moduleName, null, YdtType.SINGLE_INSTANCE_NODE);
        return builder;
    }

    private static YdtBuilder addNode(YdtBuilder builder, String path) {
        String nodeName = getPostSegment(path, COLON);
        builder.addChild(nodeName, null, YdtType.SINGLE_INSTANCE_NODE);
        return builder;
    }

    private static YdtBuilder addListOrLeafList(YdtBuilder builder, String segment) {
        String nodeName = getPreSegment(segment, EQUAL);
        String keyStr = getPostSegment(segment, EQUAL);
        if (keyStr == null) {
            throw new JsonParseException("Illegal URI, List/Leaf-list node should be in format \"nodeName=key\"" +
                    "or \"nodeName=instance-value\"");
        }
        if (keyStr.contains(COMMA)) {
            List<String> keys = Lists.newArrayList(COMMA_SPLITTER.split(keyStr));
            builder.addChild(nodeName, null);
            builder.addKeyLeafs(keys);
        } else {
            builder.addLeaf(nodeName, null, keyStr);
        }
        return builder;
    }

    private static YdtBuilder processLeaf(YdtBuilder builder, String path) {
        checkNotNull(path);
        builder.addChild(path, null);
        return builder;
    }

    private static String getPreSegment(final String path, String splitChar) {
        final int idx = path.indexOf(splitChar);
        if (idx == -1) {
            return null;
        }

        if (path.indexOf(':', idx + 1) != -1) {
            return null;
        }

        return path.substring(0, idx);
    }

    private static String getPostSegment(final String path, String splitChar) {
        final int idx = path.indexOf(splitChar);
        if (idx == -1) {
            return path;
        }

        if (path.indexOf(splitChar, idx + 1) != -1) {
            return null;
        }

        return path.substring(idx + 1);
    }

    /**
     * Converts a list of path from the original format to ISO-8859-1 code.
     *
     * @param paths the original paths
     * @return list of decoded paths
     */
    public static List<String> urlPathArgsDecode(Iterable<String> paths) {
        try {
            final List<String> decodedPathArgs = new ArrayList<String>();
            for (final String pathArg : paths) {
                final String _decode = URLDecoder.decode(pathArg, URI_ENCODING_CHAR_SET);
                decodedPathArgs.add(_decode);
            }
            return decodedPathArgs;
        } catch (final UnsupportedEncodingException e) {
            throw new JsonParseException("Invalid URL path arg '" + paths + "': ", e);
        }
    }
}
