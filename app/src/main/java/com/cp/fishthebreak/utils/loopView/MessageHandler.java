// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.cp.fishthebreak.utils.loopView;

import android.os.Handler;
import android.os.Message;

// Referenced classes of package com.qingchifan.view:
//            LoopView

final class MessageHandler extends Handler {
    public static final int WHAT_INVALIDATE_LOOP_VIEW = 1000;
    public static final int WHAT_SMOOTH_SCROLL = 2000;
    public static final int WHAT_SMOOTH_SCROLL_INERTIA = 2001;
    public static final int WHAT_ITEM_SELECTED = 3000;

    final LoopView loopview;

    MessageHandler(LoopView loopview) {
        this.loopview = loopview;
    }

    @Override
    public final void handleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_INVALIDATE_LOOP_VIEW:
                loopview.invalidate();
                break;

            case WHAT_SMOOTH_SCROLL:
                removeMessages(WHAT_SMOOTH_SCROLL_INERTIA);
                loopview.smoothScroll(LoopView.ACTION.FLING);
                break;

            case WHAT_ITEM_SELECTED:
                loopview.onItemSelected();
                break;
        }
    }

}
