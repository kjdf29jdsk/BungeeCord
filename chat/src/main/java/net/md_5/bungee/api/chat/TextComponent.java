package net.md_5.bungee.api.chat;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@AllArgsConstructor
public class TextComponent extends BaseComponent
{

    private static final Pattern url = Pattern.compile( "^(?:(https?)://)?([-\\w_\\.]{2,}\\.[a-z]{2,4})(/\\S*)?$" );

    /**
     * Converts the old formatting system that used
     * {@link net.md_5.bungee.api.ChatColor#COLOR_CHAR} into the new json based
     * system.
     *
     * @param message the text to convert
     * @return the components needed to print the message to the client
     */
    public static BaseComponent[] fromLegacyText(String message)
    {
        ArrayList<BaseComponent> components = new ArrayList<BaseComponent>();
        StringBuilder builder = new StringBuilder();
        TextComponent component = new TextComponent();
        Matcher matcher = url.matcher( message );

        for ( int i = 0; i < message.length(); i++ )
        {
            char c = message.charAt( i );
            if ( c == ChatColor.COLOR_CHAR )
            {
                i++;
                c = message.charAt( i );
                if ( c >= 'A' && c <= 'Z' )
                {
                    c += 32;
                }
                ChatColor format = ChatColor.getByChar( c );
                if ( format == null )
                {
                    continue;
                }
                if ( builder.length() > 0 )
                {
                    TextComponent old = component;
                    component = new TextComponent( old );
                    old.setText( builder.toString() );
                    builder = new StringBuilder();
                    components.add( old );
                }
                switch ( format )
                {
                    case BOLD:
                        component.setBold( true );
                        break;
                    case ITALIC:
                        component.setItalic( true );
                        break;
                    case UNDERLINE:
                        component.setUnderlined( true );
                        break;
                    case STRIKETHROUGH:
                        component.setStrikethrough( true );
                        break;
                    case MAGIC:
                        component.setObfuscated( true );
                        break;
                    case RESET:
                        format = ChatColor.WHITE;
                    default:
                        component = new TextComponent();
                        component.setColor( format );
                        break;
                }
                continue;
            }
            int pos = message.indexOf( ' ', i );
            if ( pos == -1 )
            {
                pos = message.length();
            }
            if ( matcher.region( i, pos ).find() )
            { //Web link handling

                if ( builder.length() > 0 )
                {
                    TextComponent old = component;
                    component = new TextComponent( old );
                    old.setText( builder.toString() );
                    builder = new StringBuilder();
                    components.add( old );
                }

                TextComponent old = component;
                component = new TextComponent( old );
                String urlString = message.substring( i, pos );
                component.setText( urlString );
                component.setClickEvent( new ClickEvent( ClickEvent.Action.OPEN_URL,
                        urlString.startsWith( "http" ) ? urlString : "http://" + urlString ) );
                components.add( component );
                i += pos - i - 1;
                component = old;
                continue;
            }
            builder.append( c );
        }
        if ( builder.length() > 0 )
        {
            component.setText( builder.toString() );
            components.add( component );
        }

        // The client will crash if the array is empty
        if ( components.isEmpty() )
        {
            components.add( new TextComponent( "" ) );
        }

        return components.toArray( new BaseComponent[ components.size() ] );
    }

    /**
     * The text of the component that will be displayed to the client
     */
    @NonNull private String text;

    /**
     * Creates a blank component
     */
    public TextComponent() {
        this("");
    }

    /**
     * Creates a TextComponent with formatting and text from the passed
     * component
     *
     * @param textComponent the component to copy from
     */
    public TextComponent(TextComponent textComponent)
    {
        super( textComponent );
        setText( textComponent.getText() );
    }

    /**
     * Creates a TextComponent with blank text and the extras set to the passed
     * array
     *
     * @param extras the extras to set
     */
    public TextComponent(BaseComponent... extras)
    {
        setText( "" );
        setExtra( extras );
    }

    /**
     * Creates a duplicate of this TextComponent.
     *
     * @return the duplicate of this TextComponent.
     */
    @Override
    public BaseComponent duplicate()
    {
        return new TextComponent( this );
    }

    @Override
    public BaseComponent duplicateWithoutFormatting()
    {
        return new TextComponent( this.text );
    }

    @Override
    protected void toPlainText(StringBuilder builder)
    {
        builder.append( text );
        super.toPlainText( builder );
    }

    @Override
    protected void toLegacyTextContent(ChatStringBuilder builder, ChatColor color, Set<ChatColor> decorations)
    {
        builder.format(color, decorations);
        builder.append( text );
    }

    @Override protected void toStringFirst(List<String> fields) {
        fields.add("text=\"" + getText() + "\"");
        super.toStringFirst(fields);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), text);
    }

    @Override
    protected boolean equals(BaseComponent that) {
        return that instanceof TextComponent &&
               text.equals(((TextComponent) that).getText()) &&
               super.equals(that);
    }
}
