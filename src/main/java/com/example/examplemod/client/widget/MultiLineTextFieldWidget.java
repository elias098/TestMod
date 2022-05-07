package com.example.examplemod.client.widget;

import com.example.examplemod.ExampleMod;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class MultiLineTextFieldWidget extends Widget implements IRenderable, IGuiEventListener {
    private final FontRenderer font;
    private String value = "";
    private int maxLength = 32;
    private int maxLines = -1;
    private int frame;
    private boolean bordered = true;
    private boolean canLoseFocus = true;
    private boolean isEditable = true;
    private boolean shiftPressed;
    private int cursorPos;
    private int highlightPos;
    private int textColor = 14737632;
    private int textColorUneditable = 7368816;
    private String suggestion;
    private Consumer<String> responder;
    private Predicate<String> filter = Objects::nonNull;
    private BiFunction<String, Integer, IReorderingProcessor> formatter = (p_195610_0_, p_195610_1_) -> {
        return IReorderingProcessor.forward(p_195610_0_, Style.EMPTY);
    };
    private TextDisplay displayCache;
    private long lastClickTime;
    private int lastClickIndex = -1;
    private double scrollAmount;
    private boolean scrollBarVisible = true;

    public MultiLineTextFieldWidget(FontRenderer font, int x, int y, int width, int height, ITextComponent message) {
        super(x, y, width, height, message);
        this.font = font;
    }

    public int leftPos() {
        return this.isBordered() ? this.x + 4 : this.x;
    }

    public int topPos() {
        return this.isBordered() ? this.y + 4 : this.y;
    }

    public int getInnerWidth() {
        return this.isBordered() ? this.width - 8 : this.width;
    }

    public int getInnerHeight() {
        return this.isBordered() ? this.height - 8 : this.height;
    }

    public void setResponder(Consumer<String> responder) {
        this.responder = responder;
    }

    // TODO what is this
    public void setFormatter(BiFunction<String, Integer, IReorderingProcessor> formatter) {
        this.formatter = formatter;
    }

    public void tick() {
        ++this.frame;
    }

    protected IFormattableTextComponent createNarrationMessage() {
        ITextComponent itextcomponent = this.getMessage();
        return new TranslationTextComponent("gui.narrate.editBox", itextcomponent, this.value);
    }

    // TODO add maxLines
    public void setValue(String text) {
        if (this.filter.test(text)) {
            if (text.length() > this.maxLength) {
                this.value = text.substring(0, this.maxLength);
            } else {
                this.value = text;
            }

            this.moveCursorToEnd();
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(text);
        }
    }

    public String getValue() {
        return this.value;
    }

    public String getHighlighted() {
        int selectionStart = Math.min(this.cursorPos, this.highlightPos);
        int selectionEnd = Math.max(this.cursorPos, this.highlightPos);
        return this.value.substring(selectionStart, selectionEnd);
    }

    public void setFilter(Predicate<String> filter) {
        this.filter = filter;
    }

    public void insertText(String text) {
        int selectionStart = Math.min(this.cursorPos, this.highlightPos);
        int selectionEnd = Math.max(this.cursorPos, this.highlightPos);
        int remainingLength = this.maxLength - this.value.length() - (selectionStart - selectionEnd);
        String s = this.filterText(text);
        int length = s.length();
        if (remainingLength < length) {
            s = s.substring(0, remainingLength);
            length = remainingLength;
        }

        ExampleMod.LOGGER.info(text);
        ExampleMod.LOGGER.info(s);
        String s1 = (new StringBuilder(this.value))
                .replace(selectionStart, selectionEnd, s)
                .toString();
        if (this.filter.test(s1)) {
            this.value = s1;
            this.moveCursorTo(selectionStart + length);
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(this.value);
        }
    }

    protected static boolean isAllowedChatCharacter(char c) {
        return c == '\n' || c != 167 && c >= ' ' && c != 127;
    }

    protected static String filterText(String text) {
        StringBuilder stringbuilder = new StringBuilder();

        for(char c : text.toCharArray()) {
            if (isAllowedChatCharacter(c)) {
                stringbuilder.append(c);
            }
        }

        return stringbuilder.toString();
    }

    private void onValueChange(String text) {
        if (this.responder != null) {
            this.responder.accept(text);
        }
        this.clearDisplayCache();

        this.nextNarration = Util.getMillis() + 500L;
    }

    private void deleteText(int delta) {
        if (Screen.hasControlDown()) {
            this.deleteWords(delta);
        } else {
            this.deleteChars(delta);
        }
    }

    public void deleteWords(int delta) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(delta) - this.cursorPos);
            }
        }
    }

    public void deleteChars(int delta) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int i = this.getCursorPos(delta);
                int j = Math.min(i, this.cursorPos);
                int k = Math.max(i, this.cursorPos);
                if (j != k) {
                    String s = (new StringBuilder(this.value))
                            .delete(j, k)
                            .toString();
                    if (this.filter.test(s)) {
                        this.value = s;
                        this.moveCursorTo(j);
                    }
                }
            }
        }
    }

    public int getWordPosition(int delta) {
        return this.getWordPosition(delta, this.getCursorPosition());
    }

    private int getWordPosition(int delta, int index) {
        return this.getWordPosition(delta, index, true);
    }

    // TODO add \n support
    private int getWordPosition(int delta, int index, boolean p_146197_3_) {
        int i = index;
        boolean flag = delta < 0;
        int words = Math.abs(delta);

        for(int word = 0; word < words; ++word) {
            if (!flag) {
                int l = this.value.length();
                i = this.value.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while(p_146197_3_ && i < l && this.value.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while(p_146197_3_ && i > 0 && this.value.charAt(i - 1) == ' ') {
                    --i;
                }

                while(i > 0 && this.value.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursor(int delta) {
        this.moveCursorTo(this.getCursorPos(delta));
    }

    private int getCursorPos(int delta) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, delta);
    }

    public void moveCursorTo(int index) {
        ExampleMod.LOGGER.info("move cursor");
        this.setCursorPosition(index);
        if (!this.shiftPressed) {
            this.setHighlightPos(this.cursorPos);
        }

        this.onValueChange(this.value);
        this.scrollToCursor();
        this.setScrollAmount(this.getScrollAmount());
    }

    public void setCursorPosition(int index) {
        this.cursorPos = MathHelper.clamp(index, 0, this.value.length());
    }

    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }

    public void moveCursorToEnd() {
        this.moveCursorTo(this.value.length());
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.canConsumeInput()) {
            return false;
        } else {
            this.shiftPressed = Screen.hasShiftDown();
            if (Screen.isSelectAll(keyCode)) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
                this.clearDisplayCache();
                return true;
            } else if (Screen.isCopy(keyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                return true;
            } else if (Screen.isPaste(keyCode)) {
                if (this.isEditable) {
                    this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                }

                return true;
            } else if (Screen.isCut(keyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                if (this.isEditable) {
                    this.insertText("");
                }

                return true;
            } else {
                switch(keyCode) {
                    case GLFW.GLFW_KEY_E:
                        return true;
                    case GLFW.GLFW_KEY_ENTER:
                    case GLFW.GLFW_KEY_KP_ENTER:
                        if (this.isEditable) {
                            ExampleMod.LOGGER.info("trying to add newline");
                            this.insertText("\n");
                        }
                        return true;
                    case GLFW.GLFW_KEY_BACKSPACE:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(-1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }
                        return true;
                    case GLFW.GLFW_KEY_DELETE:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }
                        return true;
                    case GLFW.GLFW_KEY_RIGHT:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(1));
                        } else {
                            this.moveCursor(1);
                        }
                        return true;
                    case GLFW.GLFW_KEY_LEFT:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }
                        return true;
                    case GLFW.GLFW_KEY_DOWN:
                        this.changeLine(1);
                        return true;
                    case GLFW.GLFW_KEY_UP:
                        this.changeLine(-1);
                        return true;
                    case GLFW.GLFW_KEY_HOME:
                        this.moveCursorToStart();
                        return true;
                    case GLFW.GLFW_KEY_END:
                        this.moveCursorToEnd();
                        return true;
                    case GLFW.GLFW_KEY_INSERT:
                    case GLFW.GLFW_KEY_PAGE_UP:
                    case GLFW.GLFW_KEY_PAGE_DOWN:
                    default:
                        return false;
                }
            }
        }
    }

    private void changeLine(int lineDelta) {
        int newIndex = this.getDisplayCache().changeLine(this.getCursorPosition(), lineDelta);
        this.moveCursorTo(newIndex);
    }

    public boolean canConsumeInput() {
        return this.isVisible() && this.isFocused() && this.isEditable();
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!this.canConsumeInput()) {
            return false;
        } else if (SharedConstants.isAllowedChatCharacter(codePoint)) {
            if (this.isEditable) {
                this.insertText(Character.toString(codePoint));
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isVisible()) {
            return false;
        } else {
            boolean flag =
                    mouseX >= (double)this.x
                    && mouseX < (double)(this.x + this.width)
                    && mouseY >= (double)this.y
                    && mouseY < (double)(this.y + this.height);
            if (this.canLoseFocus) {
                this.setFocus(flag);
            }

            if (this.isFocused() && flag && button == 0) {
                long i = Util.getMillis();
                TextDisplay textDisplay = this.getDisplayCache();
                int textIndex = textDisplay.getIndexAtPosition(
                        this.font,
                        new Point(
                                (int) mouseX - leftPos(),
                                (int) mouseY - topPos()),
                        this.getScrollAmount());
                if (textIndex >= 0) {
                    if (textIndex == this.lastClickIndex && i - this.lastClickTime < 250L) {
                        if (this.cursorPos == this.highlightPos) {
                            this.selectWord(textIndex);
                        } else {
                            this.moveCursorToEnd();
                            this.setHighlightPos(0);
                        }
                        this.clearDisplayCache();
                    } else {
                        this.moveCursorTo(textIndex);
                    }
                }

                this.lastClickIndex = textIndex;
                this.lastClickTime = i;

                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        } else if (this.visible && this.isFocused() && button == 0) {
            TextDisplay textDisplay = this.getDisplayCache();
            this.cursorPos = textDisplay.getIndexAtPosition(
                    this.font,
                    new Point(
                            (int)mouseX - leftPos(),
                            (int)mouseY - topPos()),
                    this.getScrollAmount());
            this.clearDisplayCache();

            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (this.visible
                && mouseX >= (double)this.x
                && mouseX < (double)(this.x + this.width)
                && mouseY >= (double)this.y
                && mouseY < (double)(this.y + this.height)) {
            this.setScrollAmount(this.getScrollAmount() - scroll);
            ExampleMod.LOGGER.info(this.getScrollAmount());
            return true;
        }
        return false;
    }

    public double getScrollAmount() {
        return scrollAmount;
    }

    public void setScrollAmount(double scrollAmount) {
        this.scrollAmount = MathHelper.clamp(scrollAmount, 0.0D, (double)this.getMaxScroll());
    }

    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - getInnerHeight());
    }

    protected int getMaxPosition() {
        return this.getDisplayCache().lines.length * font.lineHeight;
    }

    public void scrollToCursor() {
        TextDisplay displayCache = this.getDisplayCache();
        int topPos = this.topPos();
        int bottomPos = topPos + this.getInnerHeight();
        double cursorTop = displayCache.cursor.y - this.scrollAmount;
        double cursorBottom = cursorTop + font.lineHeight;
        ExampleMod.LOGGER.info(cursorTop);
        ExampleMod.LOGGER.info(cursorBottom);
        ExampleMod.LOGGER.info(topPos);
        ExampleMod.LOGGER.info(this.scrollAmount);
        if (cursorTop < topPos) {
            this.setScrollAmount(this.scrollAmount + cursorTop - topPos);
        } else if (cursorBottom > bottomPos) {
            ExampleMod.LOGGER.info(this.scrollAmount + cursorBottom - bottomPos);
            this.setScrollAmount(this.scrollAmount + cursorBottom - bottomPos);
        }
    }

    public void setFocus(boolean focus) {
        super.setFocused(focus);
    }

    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.isVisible()) {
            if (this.isBordered()) {
                int i = this.isFocused() ? -1 : -6250336;
                fill(matrixStack,
                        this.x - 1,
                        this.y - 1,
                        this.x + this.width + 1,
                        this.y + this.height + 1,
                        i);
                fill(matrixStack,
                        this.x,
                        this.y,
                        this.x + this.width,
                        this.y + this.height,
                        -16777216);
            }

            int textColor = this.isEditable ? this.textColor : this.textColorUneditable;
            TextDisplay textDisplay = this.getDisplayCache();


            MainWindow window = Minecraft.getInstance().getWindow();
            double scaleFactor = window.getGuiScale();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(
                    (int)(scaleFactor * this.leftPos()),
                    (int)(scaleFactor * (window.getGuiScaledHeight() - this.topPos() - this.getInnerHeight())),
                    (int)(scaleFactor * this.getInnerWidth()),
                    (int)(scaleFactor * this.getInnerHeight()));
            GL11.glPushMatrix();

            for(TextLine textLine : textDisplay.lines) {
                float lineTop = (float)textLine.y - (float)this.scrollAmount;
                float lineBottom = lineTop + font.lineHeight;
                if (lineBottom > this.topPos()
                        && lineTop < this.topPos() + this.getInnerHeight()) {
                    this.font.draw(matrixStack,
                            textLine.textComponent,
                            (float) textLine.x,
                            (float) textLine.y - (float) this.scrollAmount,
                            textColor);
                }
            }

            this.renderHighlight(textDisplay.selection);

            if (this.isFocused()) {
                this.renderCursor(matrixStack, textDisplay.cursor, textDisplay.cursorAtEnd);
            }

            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            if (this.scrollBarVisible) {
                this.renderScrollBar();
            }
        }
    }

    private void renderCursor(MatrixStack matrixStack, Point cursor, boolean endOfString) {
        if (this.frame / 6 % 2 == 0) {
            if (!endOfString) {
                int color = 0xFFE0E0E0;

                float x1 = cursor.x;
                float x2 = cursor.x + 1;
                float y1 = cursor.y - (float)this.scrollAmount - 1;
                float y2 = cursor.y - (float)this.scrollAmount + font.lineHeight;

                float f3 = (float)(color >> 24 & 255) / 255.0F;
                float f0 = (float)(color >> 16 & 255) / 255.0F;
                float f1 = (float)(color >> 8 & 255) / 255.0F;
                float f2 = (float)(color & 255) / 255.0F;

                Matrix4f matrix4f = matrixStack.last().pose();
                BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
                RenderSystem.enableBlend();
                RenderSystem.disableTexture();
                RenderSystem.defaultBlendFunc();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                bufferbuilder.vertex(matrix4f, x1, y2, 0.0F).color(f0, f1, f2, f3).endVertex();
                bufferbuilder.vertex(matrix4f, x2, y2, 0.0F).color(f0, f1, f2, f3).endVertex();
                bufferbuilder.vertex(matrix4f, x2, y1, 0.0F).color(f0, f1, f2, f3).endVertex();
                bufferbuilder.vertex(matrix4f, x1, y1, 0.0F).color(f0, f1, f2, f3).endVertex();
                bufferbuilder.end();
                WorldVertexBufferUploader.end(bufferbuilder);
                RenderSystem.enableTexture();
                RenderSystem.disableBlend();
            } else {
                this.font.draw(matrixStack,
                        "_",
                        (float)cursor.x,
                        (float)cursor.y - (float)this.scrollAmount,
                        0xE0E0E0);
            }
        }

    }

    private void renderHighlight(Rectangle2d[] selection) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);

        for(Rectangle2d rectangle2d : selection) {
            double lineTop = rectangle2d.getY() - this.scrollAmount;
            double lineBottom = lineTop + rectangle2d.getHeight();
            if (lineBottom > this.topPos()
                    && lineTop < this.topPos() + this.getInnerHeight()) {
                double x1 = rectangle2d.getX();
                double x2 = x1 + rectangle2d.getWidth();
                double y1 = rectangle2d.getY() - this.scrollAmount;
                double y2 = y1 + rectangle2d.getHeight();
                bufferbuilder.vertex(x1, y2, 0.0D).endVertex();
                bufferbuilder.vertex(x2, y2, 0.0D).endVertex();
                bufferbuilder.vertex(x2, y1, 0.0D).endVertex();
                bufferbuilder.vertex(x1, y1, 0.0D).endVertex();
            }
        }

        tessellator.end();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    private void renderScrollBar() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        int maxScroll = this.getMaxScroll();

        int borderOffset = this.isBordered() ? 1 : 0;
        int innerHeight = this.height - 2 * borderOffset;
        int x1 = this.x + this.width - borderOffset - 6;
        int x2 = this.x + this.width - borderOffset;
        int y1 = this.y + borderOffset;
        int y2 = this.y + this.height - borderOffset;

        RenderSystem.disableTexture();
        float scrollerSize = (float)(innerHeight * innerHeight) / (float)this.getMaxPosition();
        scrollerSize = MathHelper.clamp(scrollerSize, 8, innerHeight);
        float scrollerPos = y1;
        if (maxScroll > 0) {
            scrollerPos += this.getScrollAmount() * (innerHeight - scrollerSize) / maxScroll;
        }

        // Scrollbar
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.vertex((double)x1, (double)y2, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex((double)x2, (double)y2, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex((double)x2, (double)y1, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
        bufferbuilder.vertex((double)x1, (double)y1, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();

        // Scroller
        bufferbuilder.vertex((double)x1, (double)(scrollerPos + scrollerSize), 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
        bufferbuilder.vertex((double)x2, (double)(scrollerPos + scrollerSize), 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
        bufferbuilder.vertex((double)x2, (double)scrollerPos, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
        bufferbuilder.vertex((double)x1, (double)scrollerPos, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();

        // Scroller shading
        bufferbuilder.vertex((double)x1, (double)(scrollerPos + scrollerSize - 1), 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
        bufferbuilder.vertex((double)(x2 - 1), (double)(scrollerPos + scrollerSize - 1), 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
        bufferbuilder.vertex((double)(x2 - 1), (double)scrollerPos, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
        bufferbuilder.vertex((double)x1, (double)scrollerPos, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
        tessellator.end();
    }

    public void setScrollBarVisible(boolean visibility) {
        this.scrollBarVisible = visibility;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (this.value.length() > maxLength) {
            this.value = this.value.substring(0, maxLength);
            this.onValueChange(this.value);
        }
    }

    private int getMaxLength() {
        return this.maxLength;
    }

    // TODO handle removing lines
    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    private int getMaxLines() {
        return this.maxLines;
    }

    public int getCursorPosition() {
        return this.cursorPos;
    }

    private boolean isBordered() {
        return this.bordered;
    }

    public void setBordered(boolean bordered) {
        this.bordered = bordered;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setTextColorUneditable(int textColorUneditable) {
        this.textColorUneditable = textColorUneditable;
    }

    public boolean changeFocus(boolean focus) {
        return this.visible && this.isEditable && super.changeFocus(focus);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible
                && mouseX >= (double)this.x
                && mouseX < (double)(this.x + this.width)
                && mouseY >= (double)this.y
                && mouseY < (double)(this.y + this.height);
    }

    protected void onFocusedChanged(boolean focused) {
        if (focused) {
            this.frame = 0;
        }

    }

    private boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    public void setHighlightPos(int index) {
        int i = this.value.length();
        this.highlightPos = MathHelper.clamp(index, 0, i);
    }

    public void setCanLoseFocus(boolean canLoseFocus) {
        this.canLoseFocus = canLoseFocus;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setSuggestion(@Nullable String suggestion) {
        this.suggestion = suggestion;
    }

    private void selectWord(int i) {
        String s = this.value;
        this.moveCursorTo(CharacterManager.getWordPosition(s, 1, i, false));
        this.setHighlightPos(CharacterManager.getWordPosition(s, -1, i, false));
        this.clearDisplayCache();
    }

    private TextDisplay getDisplayCache() {
        if (this.displayCache == null) {
            this.displayCache = this.rebuildDisplayCache();
        }

        return this.displayCache;
    }

    private void clearDisplayCache() {
        this.displayCache = null;
    }
    
    private TextDisplay rebuildDisplayCache() {
        String fullText = this.value;
        if (fullText.isEmpty()) {
            return TextDisplay.EMPTY;
        } else {
            int cursorIndex = this.cursorPos;
            int selectionStartIndex = this.highlightPos;
            IntList intlist = new IntArrayList();
            List<TextLine> lines = Lists.newArrayList();
            MutableInt mutableint = new MutableInt();
            MutableBoolean mutableboolean = new MutableBoolean();
            CharacterManager charactermanager = this.font.getSplitter();
            charactermanager.splitLines(fullText, /*maxWidth*/ this.getInnerWidth(), Style.EMPTY, true,
                    (style, sliceStart, sliceEnd) -> {
                        int line = mutableint.getAndIncrement();
                        String textSlice = fullText.substring(sliceStart, sliceEnd);
                        mutableboolean.setValue(textSlice.endsWith("\n"));
                        String textSliceFinal = StringUtils.stripEnd(textSlice, " \n");
                        Point linePoint = new Point(0, topPos() + line * font.lineHeight);
                        intlist.add(sliceStart);
                        lines.add(new TextLine(style, textSliceFinal, 
                                leftPos(),
                                topPos() + line * font.lineHeight));
                    });
            int[] lineStarts = intlist.toIntArray();
            boolean cursorAtEnd = cursorIndex == fullText.length();
            Point cursor;
            if (cursorAtEnd && mutableboolean.isTrue()) {
                cursor = new Point(leftPos(), topPos() + lines.size() * font.lineHeight);
            } else {
                int line = findLineFromPos(lineStarts, cursorIndex);
                int cursorX = leftPos() + this.font.width(fullText.substring(lineStarts[line], cursorIndex));
                cursor = new Point(cursorX, topPos() + line * font.lineHeight);
            }

            List<Rectangle2d> selection = Lists.newArrayList();
            if (cursorIndex != selectionStartIndex) {
                int selectionStart = Math.min(cursorIndex, selectionStartIndex);
                int selectionEnd = Math.max(cursorIndex, selectionStartIndex);
                int selectionStartLine = findLineFromPos(lineStarts, selectionStart);
                int selectionEndLine = findLineFromPos(lineStarts, selectionEnd);
                if (selectionStartLine == selectionEndLine) {
                    selection.add(this.createPartialLineSelection(fullText, charactermanager,
                            selectionStart, 
                            selectionEnd,
                            selectionStartLine * font.lineHeight,
                            lineStarts[selectionStartLine]));
                } else {
                    int firstLineEndIndex = selectionStartLine + 1 > lineStarts.length
                            ? fullText.length()
                            : lineStarts[selectionStartLine + 1];
                    selection.add(this.createPartialLineSelection(fullText, charactermanager,
                            selectionStart, 
                            firstLineEndIndex, 
                            selectionStartLine * font.lineHeight,
                            lineStarts[selectionStartLine]));

                    for(int line = selectionStartLine + 1; line < selectionEndLine; ++line) {
                        int y = line * font.lineHeight;
                        String lineText = fullText.substring(lineStarts[line], lineStarts[line + 1]);
                        int endX = (int)charactermanager.stringWidth(lineText);
                        selection.add(this.createSelection(
                                new Point(0, y),
                                new Point(endX, y + font.lineHeight)));
                    }

                    selection.add(this.createPartialLineSelection(fullText, charactermanager,
                            lineStarts[selectionEndLine], 
                            selectionEnd,
                            selectionEndLine * font.lineHeight,
                            lineStarts[selectionEndLine]));
                }
            }

            return new TextDisplay(fullText, cursor, cursorAtEnd, lineStarts,
                    lines.toArray(new TextLine[0]), selection.toArray(new Rectangle2d[0]));
        }
    }
    
    private static int findLineFromPos(int[] intArray, int intIn) {
        int i = Arrays.binarySearch(intArray, intIn);
        return i < 0 ? -(i + 2) : i;
    }
    
    private Rectangle2d createPartialLineSelection(String text, CharacterManager characterManager,
                                                   int selectionStart, int selectionEnd,
                                                   int selectionStartY, int lineStartIndex) {
        String testToStart = text.substring(lineStartIndex, selectionStart);
        String textToEnd = text.substring(lineStartIndex, selectionEnd);
        Point selectionStartPoint = new Point((int)characterManager
                .stringWidth(testToStart), selectionStartY);
        Point selectionEndPoint = new Point((int)characterManager
                .stringWidth(textToEnd), selectionStartY + 9);
        return this.createSelection(selectionStartPoint, selectionEndPoint);
    }

    private Rectangle2d createSelection(Point startPoint, Point endPoint) {
        int x1 = Math.min(startPoint.x, endPoint.x);
        int x2 = Math.max(startPoint.x, endPoint.x);
        int y1 = Math.min(startPoint.y, endPoint.y);
        int y2 = Math.max(startPoint.y, endPoint.y);
        return new Rectangle2d(
                leftPos() + x1,
                topPos() + y1,
                x2 - x1,
                y2 - y1);
    }
    
    static class TextLine {
        private final Style style;
        private final String string;
        private final ITextComponent textComponent;
        private final int x;
        private final int y;

        public TextLine(Style style, String string, int x, int y) {
            this.style = style;
            this.string = string;
            this.x = x;
            this.y = y;
            this.textComponent = (new StringTextComponent(string)).setStyle(style);
        }
    }
    
    static class TextDisplay {
        private static final TextDisplay EMPTY =
                new TextDisplay(
                        "",
                        new Point(0, 0),
                        true,
                        new int[]{0},
                        new TextLine[]{
                                new TextLine(
                                        Style.EMPTY,
                                        "",
                                        0, 0)},
                        new Rectangle2d[0]);
        List<String> inputList = new ArrayList<String>();
        private final String fullText;
        private final Point cursor;
        private final boolean cursorAtEnd;
        private final int[] lineStarts;
        private final TextLine[] lines;
        private final Rectangle2d[] selection;

        public TextDisplay(String fullText, Point cursor, Boolean cursorAtEnd,
                           int[] lineStarts, TextLine[] lines, Rectangle2d[] selection) {
            this.fullText = fullText;
            this.cursor = cursor;
            this.cursorAtEnd = cursorAtEnd;
            this.lineStarts = lineStarts;
            this.lines = lines;
            this.selection = selection;
        }

        public int getIndexAtPosition(FontRenderer font, Point point, double scrollAmount) {
            int i = (point.y + (int) scrollAmount) / font.lineHeight;
            if (i < 0) {
                return 0;
            } else if (i >= this.lines.length) {
                return this.fullText.length();
            } else {
                TextLine textLine = this.lines[i];
                return this.lineStarts[i] + font.getSplitter()
                        .plainIndexAtWidth(textLine.string, point.x, textLine.style);
            }
        }

        public int changeLine(int cursorIndex, int lineDelta) {
            int line = findLineFromPos(this.lineStarts, cursorIndex);
            int newLine = line + lineDelta;
            int newCursorIndex;
            if (newLine < 0) {
                newCursorIndex = 0;
            } else if (newLine < this.lineStarts.length) {
                int cursorLineIndex = cursorIndex - this.lineStarts[line];
                int newLineEndIndex = this.lines[newLine].string.length();
                newCursorIndex = this.lineStarts[newLine] + Math.min(cursorLineIndex, newLineEndIndex);
            } else {
                newCursorIndex = this.fullText.length();
            }

            return newCursorIndex;
        }

        public int findLineStart(int cursorIndex) {
            int line = findLineFromPos(this.lineStarts, cursorIndex);
            return this.lineStarts[line];
        }

        public int findLineEnd(int cursorIndex) {
            int line = findLineFromPos(this.lineStarts, cursorIndex);
            return this.lineStarts[line] + this.lines[line].string.length();
        }
    }

    static class Point {
        public final int x;
        public final int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}