package com.ssafy.cotrip.global.util;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
public class SliceService {

    public <T, C> SliceResponse<T, C> toSliceResponse(
            List<T> items,
            int size,
            Function<T, C> cursorExtractor
    ) {
        boolean hasNext = items.size() > size;

        List<T> content = items;
        if (hasNext) {
            content = items.subList(0, size);
        }

        C nextCursor = null;
        if (!content.isEmpty()) {
            T last = content.get(content.size() - 1);
            nextCursor = cursorExtractor.apply(last);
        }

        return SliceResponse.<T, C>builder()
                .content(content)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }
}

