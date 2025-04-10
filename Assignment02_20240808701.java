// Deniz Buyuksahin
// 20240808701
// CSE102 Assignment 2

// Custom Exception Classes
class EquipmentBrokenException extends Exception {
    public EquipmentBrokenException(String message) {
        super(message);
    }
}

class EquipmentFullException extends Exception {
    public EquipmentFullException(String message) {
        super("Error: " + message);
    }
}

class BonusNotAllowedException extends Exception {
    public BonusNotAllowedException(String message) {
        super("Error: " + message);
    }
}

class InventoryFullException extends Exception {
    public InventoryFullException(String message) {
        super("Error: " + message);
    }
}

// Item Class
class Item {
    private String name;
    private String bonusType;
    private int bonusValue;

    public Item(String name, String bonusType, int bonusValue) {
        this.name = name;
        this.bonusType = bonusType;
        this.bonusValue = bonusValue;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBonusType() { return bonusType; }
    public void setBonusType(String bonusType) { this.bonusType = bonusType; }
    public int getBonusValue() { return bonusValue; }
    public void setBonusValue(int bonusValue) { this.bonusValue = bonusValue; }

    public void reduceBonus(int amount) throws EquipmentBrokenException {
        if (amount >= bonusValue) {
            bonusValue = 0;
            throw new EquipmentBrokenException(name + " has broken!");
        } else {
            bonusValue -= amount;
        }
    }

    @Override
    public String toString() {
        return name + " [" + bonusType + " Bonus: " + bonusValue + "]";
    }
}

// Abstract Character Class
abstract class Character {
    protected String name;
    private int health;
    private int attackPower;
    private int maxHealth;
    private Item[] inventory;
    private int inventoryCapacity;
    private Item[] equipment;
    private int equipmentCapacity;
    private String notifications;

    public Character(String name, int health, int attackPower, 
                   int inventoryCapacity, int equipmentCapacity) {
        this.name = name;
        this.health = health;
        this.attackPower = attackPower;
        this.maxHealth = health;
        this.inventory = new Item[inventoryCapacity];
        this.equipment = new Item[equipmentCapacity];
        this.inventoryCapacity = inventoryCapacity;
        this.equipmentCapacity = equipmentCapacity;
        this.notifications = "";
    }

    public void addNotification(String msg) {
        notifications += msg + "\n";
        System.out.println(msg);
    }

    public void printNotifications() {
        if (!notifications.isEmpty()) {
            System.out.print(notifications);
            notifications = "";
        }
    }

    // Abstract methods
    public abstract String getStatString();
    public abstract int getTotalArmor();
    public abstract int getTotalBaseArmor();
    protected abstract boolean isBonusAllowed(String bonusType);

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
    public int getAttackPower() { return attackPower; }
    public void setAttackPower(int attackPower) { this.attackPower = attackPower; }
    public int getMaxHealth() { return maxHealth; }
    public Item[] getInventory() { return inventory; }
    public Item[] getEquipment() { return equipment; }

    public void printCurrentStats() {
        System.out.println(name + " - " + getStatString());
        System.out.println("Current Health: " + health + "/" + maxHealth);
        printNotifications();
    }

    protected void takeHealthDamage(int damage) {
        health = Math.max(0, health - damage);
        System.out.println(name + " takes " + damage + " damage. (Health: " + health + ")");
    }

    public void receiveDamage(int damage, boolean ignoreArmor) {
        if (ignoreArmor || getTotalArmor() == 0) {
            takeHealthDamage(damage);
        } else {
            double reductionPercent = (getTotalArmor() >= 50) ? 0.10 : 0.05;
            int reducedDamage = (int) Math.round(damage * (1 - reductionPercent));
            System.out.println("Damage reduced by " + (reductionPercent * 100) + "%");
            baseArmorReduction();
            takeHealthDamage(reducedDamage);
        }
    }

    protected int baseArmorReduction() {
        return 0;
    }

    // Inventory/Equipment Methods
    public boolean addItemToInventory(Item item) {
        try {
            for (int i = 0; i < inventory.length; i++) {
                if (inventory[i] == null) {
                    inventory[i] = item;
                    addNotification(name + " added " + item.getName() + " to Inventory. (" + 
                                  item.getBonusType() + " Bonus: " + item.getBonusValue() + ")");
                    return true;
                }
            }
            throw new InventoryFullException(name + "'s inventory is full!");
        } catch (InventoryFullException e) {
            addNotification(e.getMessage());
            return false;
        }
    }

    public Item removeItemFromInventory(int index) {
        if (index < 0 || index >= inventory.length || inventory[index] == null) {
            return null;
        }
        Item removed = inventory[index];
        // Shift items
        for (int i = index; i < inventory.length - 1; i++) {
            inventory[i] = inventory[i + 1];
        }
        inventory[inventory.length - 1] = null;
        return removed;
    }

    public boolean equipItemFromInventory(int invIndex) {
        if (invIndex < 0 || invIndex >= inventory.length || inventory[invIndex] == null) {
            return false;
        }
        
        Item item = inventory[invIndex];
        try {
            equipItem(item);
            removeItemFromInventory(invIndex);
            return true;
        } catch (EquipmentFullException | BonusNotAllowedException e) {
            addNotification(e.getMessage());
            return false;
        }
    }

    public boolean unequipItem(int equipIndex) {
        if (equipIndex < 0 || equipIndex >= equipment.length || equipment[equipIndex] == null) {
            return false;
        }
        
        Item item = equipment[equipIndex];
        if (addItemToInventory(item)) {
            equipment[equipIndex] = null;
            addNotification(name + " unequipped " + item.getName());
            return true;
        }
        return false;
    }

    public int getEquipmentBonus(String bonusType) {
        int total = 0;
        for (Item item : equipment) {
            if (item != null && item.getBonusType().equals(bonusType)) {
                total += item.getBonusValue();
            }
        }
        return total;
    }

    public void equipItem(Item item) throws EquipmentFullException, BonusNotAllowedException {
        if (!isBonusAllowed(item.getBonusType())) {
            throw new BonusNotAllowedException(item.getBonusType() + " bonus type is not allowed for " + name);
        }
        
        for (int i = 0; i < equipment.length; i++) {
            if (equipment[i] == null) {
                equipment[i] = item;
                addNotification(name + " equipped " + item.getName() + " (" + 
                              item.getBonusType() + " Bonus: " + item.getBonusValue() + ")");
                return;
            }
        }
        
        throw new EquipmentFullException(name + "'s equipment slots are full!");
    }

    protected int consumeResource(String resourceType, int cost, int baseResource) {
        int bonus = getEquipmentBonus(resourceType);
        if (bonus >= cost) {
        try {
            consumeResourceBonus(resourceType, cost);
            return baseResource;
        } catch (EquipmentBrokenException e) {
            addNotification(e.getMessage());
        }
    }
    return baseResource - (cost - bonus);
}


    /**
     * @param resourceType
     * @param cost
     * @throws EquipmentBrokenException
     */
    protected void consumeResourceBonus(String resourceType, int cost) throws EquipmentBrokenException {
        int remaining = cost;
        for (int i = 0; i < equipment.length && remaining > 0; i++) {
            if (equipment[i] != null && equipment[i].getBonusType().equals(resourceType)) {
                try {
                    equipment[i].reduceBonus(remaining);
                    remaining = 0;
                } catch (EquipmentBrokenException e) {
                    addNotification(e.getMessage());
                    remaining -= equipment[i].getBonusValue();
                    equipment[i] = null; // Kırılan ekipmanı tamamen kaldır
                }
            }
        }
    }

    public void removeEquipmentAtIndex(int index) {
        if (index < 0 || index >= equipment.length) return;
        
        // Shift items
        for (int i = index; i < equipment.length - 1; i++) {
            equipment[i] = equipment[i + 1];
        }
        equipment[equipment.length - 1] = null;
    }

    // Combat methods
    public abstract void attack(Character target);
    public abstract void specialAttack(Character target);

    @Override
public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(name).append(" (Health: ").append(health).append("/").append(maxHealth)
      .append(", ").append(getStatString()).append(" | Equipment: ");
    
    boolean hasValidEquipment = false;
    for (Item item : equipment) {
        if (item != null && item.getBonusValue() > 0) { // Sadece bonus değeri >0 olanları göster
            sb.append(item).append(", ");
            hasValidEquipment = true;
        }
    }
    
    if (!hasValidEquipment) {
        sb.append("None");
    } else {
        sb.setLength(sb.length() - 2); // Son virgülü ve boşluğu sil
    }
    
    sb.append(")");
    return sb.toString();
    }
}

// Warrior Class
class Warrior extends Character {
    private int baseArmor;
    private int energy;

    public Warrior(String name, int health, int attackPower, int baseArmor) {
        super(name, health, attackPower, 5, 4);
        this.baseArmor = baseArmor;
        this.energy = 50;
    }

    @Override
    public int getTotalArmor() {
        return baseArmor + getEquipmentBonus("ARMOR");
    }

    @Override
    public int getTotalBaseArmor() {
        return baseArmor;
    }

    @Override
    protected boolean isBonusAllowed(String bonusType) {
        return bonusType.equals("ATTACK") || bonusType.equals("ARMOR") || bonusType.equals("ENERGY");
    }

    @Override
    protected int baseArmorReduction() {
    int reduction = 20;
    if (baseArmor >= reduction) {
        baseArmor -= reduction;
    } else {
        reduction = baseArmor;
        baseArmor = 0;
    }
    
    // Steel Shield kırılırsa equipment'ten kaldır
    try {
        consumeResourceBonus("ARMOR", reduction);
    } catch (EquipmentBrokenException e) {
        addNotification(e.getMessage());
    }
    
    return reduction;
}

    @Override
    public void attack(Character target) {
        int availableEnergy = energy + getEquipmentBonus("ENERGY");
        if (availableEnergy < 10) {
            addNotification(name + " doesn't have enough energy for an attack!");
            return;
        }

        energy = consumeResource("ENERGY", 10, energy);
        int damage = getAttackPower() + getEquipmentBonus("ATTACK");
        
        addNotification(name + " performs a basic attack (Energy reduced by: 10)");
        target.receiveDamage(damage, false);
        printCurrentStats();
    }

    @Override
    public void specialAttack(Character target) {
        int availableEnergy = energy + getEquipmentBonus("ENERGY");
        if (availableEnergy < 15) {
            addNotification(name + " doesn't have enough energy for a special attack! Using basic attack instead.");
            attack(target);
            return;
        }

        energy = consumeResource("ENERGY", 15, energy);
        int damage = getAttackPower() + getEquipmentBonus("ATTACK") + 5;
        
        addNotification(name + " uses Shield Bash on " + target.getName() + " (Energy reduced by: 15)");
        target.receiveDamage(damage, false);
        printCurrentStats();
    }

    @Override
    public String getStatString() {
        return "Total Attack: " + (getAttackPower() + getEquipmentBonus("ATTACK")) + 
               ", Total Armor: " + getTotalArmor() + 
               ", Total Energy: " + (energy + getEquipmentBonus("ENERGY"));
    }
}

// Mage Class
class Mage extends Character {
    private int mana;

    public Mage(String name, int health, int attackPower, int mana) {
        super(name, health, attackPower, 2, 1);
        this.mana = mana;
    }

    @Override
    protected boolean isBonusAllowed(String bonusType) {
        return bonusType.equals("ATTACK") || bonusType.equals("MANA");
    }

    @Override
    public int getTotalArmor() {
        return 0;
    }

    @Override
    public int getTotalBaseArmor() {
        return 0;
    }

    @Override
    public void attack(Character target) {
        int availableMana = mana + getEquipmentBonus("MANA");
        if (availableMana < 10) {
            // Weak attack
            int damage = getAttackPower() / 2;
            addNotification(name + " casts a weak spell due to low mana");
            target.receiveDamage(damage, false);
            return;
        }

        mana = consumeResource("MANA", 10, mana);
        int damage = getAttackPower() + getEquipmentBonus("ATTACK");
        
        addNotification(name + " casts a basic spell on " + target.getName() + " (Mana reduced by: 10)");
        target.receiveDamage(damage, false);
        printCurrentStats();
    }

    @Override
    public void specialAttack(Character target) {
        int availableMana = mana + getEquipmentBonus("MANA");
        if (availableMana < 15) {
            addNotification(name + " doesn't have enough mana for a special attack! Using basic attack instead.");
            attack(target);
            return;
        }

        mana = consumeResource("MANA", 15, mana);
        int damage = getAttackPower() + getEquipmentBonus("ATTACK") + 10;
        
        boolean ignoreArmor = target instanceof Warrior;
        if (ignoreArmor) {
            addNotification("This attack ignores armor! Full damage applied.");
        }
        
        addNotification(name + " uses a penetrating spell on " + target.getName() + " (Mana reduced by: 15)");
        target.receiveDamage(damage, ignoreArmor);
        printCurrentStats();
    }

    @Override
    public String getStatString() {
        return "Total Attack: " + (getAttackPower() + getEquipmentBonus("ATTACK")) + 
               ", Total Mana: " + (mana + getEquipmentBonus("MANA"));
    }
}

// Archer Class
class Archer extends Character {
    private int accuracy;
    private int energy;

    public Archer(String name, int health, int attackPower, int accuracy) {
        super(name, health, attackPower, 4, 3);
        this.accuracy = accuracy;
        this.energy = 50;
    }

    @Override
    protected boolean isBonusAllowed(String bonusType) {
        return bonusType.equals("ATTACK") || bonusType.equals("ACCURACY") || bonusType.equals("ENERGY");
    }

    @Override
    public int getTotalArmor() {
        return 0;
    }

    @Override
    public int getTotalBaseArmor() {
        return 0;
    }

    @Override
    public void attack(Character target) {
        int availableEnergy = energy + getEquipmentBonus("ENERGY");
        if (availableEnergy < 10) {
            // Weak attack
            int damage = getAttackPower() / 2;
            addNotification(name + " fires a weak shot due to low energy");
            target.receiveDamage(damage, false);
            return;
        }

        energy = consumeResource("ENERGY", 10, energy);
        int damage = getAttackPower() + getEquipmentBonus("ATTACK");
        
        // Critical hit chance
        int totalAccuracy = accuracy + getEquipmentBonus("ACCURACY");
        if (totalAccuracy >= 80) {
            damage += 5;
            addNotification(name + " lands a critical hit!");
        }
        
        addNotification(name + " fires an arrow at " + target.getName() + " (Energy reduced by: 10)");
        target.receiveDamage(damage, false);
        printCurrentStats();
    }

    @Override
    public void specialAttack(Character target) {
        int availableEnergy = energy + getEquipmentBonus("ENERGY");
        if (availableEnergy < 15) {
            addNotification(name + " doesn't have enough energy for a special attack! Using basic attack instead.");
            attack(target);
            return;
        }

        energy = consumeResource("ENERGY", 15, energy);
        int damage = getAttackPower() + getEquipmentBonus("ATTACK") + 5;
        
        // Critical hit chance
        int totalAccuracy = accuracy + getEquipmentBonus("ACCURACY");
        if (totalAccuracy >= 80) {
            damage += 5;
            addNotification(name + " lands a critical hit!");
        }
        
        boolean ignoreArmor = target instanceof Warrior;
        if (ignoreArmor) {
            addNotification("This attack ignores armor! Full damage applied.");
        }
        
        addNotification(name + " performs a critical arrow shot on " + target.getName() + " (Energy reduced by: 15)");
        target.receiveDamage(damage, ignoreArmor);
        printCurrentStats();
    }

    @Override
    public String getStatString() {
        return "Total Attack: " + (getAttackPower() + getEquipmentBonus("ATTACK")) + 
               ", Total Accuracy: " + (accuracy + getEquipmentBonus("ACCURACY")) + 
               ", Total Energy: " + (energy + getEquipmentBonus("ENERGY"));
    }
}

public class Assignment02_20240808701 {
    public static void main(String[] args) {
        Warrior warrior = new Warrior("Conan", 100, 15, 60);
        Mage mage = new Mage("Merlin", 90, 12, 40);
        Archer archer = new Archer("Robin", 85, 16, 65);

        // Pre-Battle: Equip items.
        // warrior: "Steel Shield" (ARMOR), "Power Bracer" (ENERGY), "Sword of Might" (ATTACK)
        warrior.addItemToInventory(new Item("Steel Shield", "ARMOR", 20));
        warrior.addItemToInventory(new Item("Power Bracer", "ENERGY", 10));
        warrior.addItemToInventory(new Item("Sword of Might", "ATTACK", 5));
        warrior.equipItemFromInventory(0);
        warrior.equipItemFromInventory(1);
        warrior.equipItemFromInventory(2);

        // Mage: "Mana Pendant" (MANA)
        mage.addItemToInventory(new Item("Mana Pendant", "MANA", 8));
        mage.equipItemFromInventory(0);

        //Archers: "Eagle Eye" (ACCURACY), "Energy Booster" (ENERGY)
        archer.addItemToInventory(new Item("Eagle Eye", "ACCURACY", 10));
        archer.addItemToInventory(new Item("Energy Booster", "Energy", 8));
        archer.equipItemFromInventory(0);
        archer.equipItemFromInventory(1);

        System.out.println("\n=== Combat Simulation ===");
        
        System.out.println("\n--- Round 1 ---");
        // Round 1:
        // Warrior special attacks Mage.
        warrior.specialAttack(mage);
        // Mage basic attacks Warrior.
        mage.attack(archer);
        // Archer special attacks Warrior.
        archer.specialAttack(warrior);

        System.out.println("\n--- Round 2 ---");
        // Round 2:
        // Warrior basic attacks Archer.
        warrior.attack(archer);
        // Mage special attacks Warrior.
        mage.specialAttack(warrior);
        // Archer basic attacks Warrior.
        archer.attack(warrior);

        System.out.println("\n=== Final Status ===");
        System.out.println(warrior);
        System.out.println(mage);
        System.out.println(archer);

        System.out.println("\n=== Error Messages Test ===");
        // Create a small Mafe for testingg (Inventory: 2, Equipment: 1)
        Mage testMage = new Mage("TestMage", 50, 10, 30);

        // Try to add 3 items to inventory.
        testMage.addItemToInventory(new Item("Item1", "MANA", 5));
        testMage.addItemToInventory(new Item("Item2", "Attack", 3));
        // Third addition should trigger an error.
        testMage.addItemToInventory(new Item("Item3", "MANA", 4));

        // Try to quip more than capacity.
        // Since testMage's equipment capacity is 1, equipping a second item should trigger an error.
        testMage.equipItemFromInventory(0); // Equip first item.
        testMage.equipItemFromInventory(0); // Try to equip second item.

        Archer testArcher = new Archer("Robin", 85, 16, 65);
        // Try to equip an item with an invalid bonus type.
        testArcher.addItemToInventory(new Item("Item4", "MANA", 4));
        testArcher.equipItemFromInventory(0); // Equip attempt should display an error.
    }
}
