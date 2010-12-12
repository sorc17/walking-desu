package common.messages;

import common.Message;
import common.MessageType;
import common.items.Items;
import java.io.Serializable;

/**
 * Message what nofity client what etc item added to his inventory.
 * @author CatsPaw
 */
public class InventoryAddEtcItem implements Message, Serializable {

    /**
     * Message type.
     */
    private final MessageType type = MessageType.INVENTORYADDETCITEM;
    /**
     * Id of etc item.
     */
    private long id;
    /**
     * Count of etc item.
     */
    private int count;
    /**
     * Type of etc item.
     */
    private Items itemType;

    /**
     * Creates new message.
     * @param id id of etc item.
     * @param count count of etc item.
     * @param itemType type of etc item.
     */
    public InventoryAddEtcItem(long id, int count, Items itemType) {
        this.id = id;
        this.count = count;
        this.itemType = itemType;
    }

    /**
     * Returns message type.
     * @return message type.
     */
    @Override
    public MessageType getType() {
        return type;
    }

    /**
     * Returns etc item type.
     * @return etc item type.
     */
    public Items getItemType() {
        return itemType;
    }

    /**
     * Returns id of etc item.
     * @return id of etc item.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns count of etc item.
     * @return count of etc item.
     */
    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "Etc item " + itemType.name() + "(" + count + ") id=" + getId() + " added to inventory.";
    }
}
