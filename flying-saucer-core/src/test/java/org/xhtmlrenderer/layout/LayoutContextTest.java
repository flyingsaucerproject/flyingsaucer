package org.xhtmlrenderer.layout;

import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.extend.FontContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LayoutContextTest {
    private final SharedContext sharedContext = mock();
    private final FontContext fontContext = mock();
    private final LayoutContext c = new LayoutContext(sharedContext, fontContext);

    /**
     * See <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/612">issue 612</a>
     */
    @Test
    void restoresCapturedPaginationState() {
        when(sharedContext.isPrint()).thenReturn(true);
        c.setPageName("first");
        c.setExtraSpaceTop(910);
        c.setExtraSpaceBottom(90);
        c.setNoPageBreak(2);

        LayoutState state = c.captureLayoutState();
        assertThat(state.getExtraSpaceTop()).isEqualTo(910);
        assertThat(state.getExtraSpaceBottom()).isEqualTo(90);
        assertThat(state.getPageName()).isEqualTo("first");
        assertThat(state.getNoPageBreak()).isEqualTo(2);

        c.setPageName("second");
        c.setExtraSpaceTop(1);
        c.setExtraSpaceBottom(3);
        c.setNoPageBreak(0);
        c.restoreLayoutState(state);

        assertThat(c.getPageName()).isEqualTo("first");
        assertThat(c.getExtraSpaceTop()).isEqualTo(910);
        assertThat(c.getExtraSpaceBottom()).isEqualTo(90);
        assertThat(c.getNoPageBreak()).isEqualTo(2);
    }
}
