// Assignment2.java
// CSE102 Assignment 2 - Game Characters and Equipment System
// This file contains all the required classes and exceptions for the game system.

// Custom Exception Classes
class EquipmentBrokenException extends Exception {
    public EquipmentBrokenException(String message) {
        super(message);
    }
}

class EquipmentFullException extends Exception {
    public EquipmentFullException(String message) {
        super("Error:" + message);
    }
}

class BonusNotAllowedException extends Exception {
    public BonusNotAllowedException(String message) {
        super("Error:" + message);
    }
}

class InventoryFullException extends Exception {
    public InventoryFullException(String message) {
        super("Error:" + message);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBonusType() {
        return bonusType;
    }

    public void setBonusType(String bonusType) {
        this.bonusType = bonusType;
    }

    public int getBonusValue() {
        return bonusValue;
    }

    public void setBonusValue(int bonusValue) {
        this.bonusValue = bonusValue;
    }

    public void reduceBonus(int amount) throws EquipmentBrokenException {
        if (amount >= bonusValue) {
            bonusValue = 0;
            throw new EquipmentBrokenException(name + "'s bonus has been depleted!");
        } else {
            bonusValue -= amount;
        }
    }

    public String toString() {
        return name + " [" + bonusType + " Bonus: " + bonusValue + "]";
    }
}

// Abstract Character Class
abstract class Character {
    private String name;
    private int health;
    private int attackPower;
    private int maxHealth;
    private Item[] inventory;
    private int inventoryCapacity;
    private Item[] equipment;
    private int equipmentCapacity;
    private String notifications;

    public Character(String name, int health, int attackPower, int inventoryCapacity, int equipmentCapacity) {
        this.name = name;
        this.health = health;
        this.attackPower = attackPower;
        this.maxHealth = health;
        this.inventoryCapacity = inventoryCapacity;
        this.equipmentCapacity = equipmentCapacity;
        this.inventory = new Item[inventoryCapacity];
        this.equipment = new Item[equipmentCapacity];
        this.notifications = "";
    }

    public void addNotification(String msg) {
        notifications += msg + "\n";
    }

    public void printNotifications() {
        if (!notifications.isEmpty()) {
            System.out.print(notifications);
            notifications = "";
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public Item[] getInventory() {
        return inventory;
    }

    public void setInventory(Item[] inventory) {
        this.inventory = inventory;
    }

    public int getInventoryCapacity() {
        return inventoryCapacity;
    }

    public void setInventoryCapacity(int inventoryCapacity) {
        this.inventoryCapacity = inventoryCapacity;
    }

    public Item[] getEquipment() {
        return equipment;
    }

    public void setEquipment(Item[] equipment) {
        this.equipment = equipment;
    }

    public int getEquipmentCapacity() {
        return equipmentCapacity;
    }

    public void setEquipmentCapacity(int equipmentCapacity) {
        this.equipmentCapacity = equipmentCapacity;
    }

    public abstract String getStatString();
    public abstract int getTotalArmor();
    public abstract int getTotalBaseArmor();

    public void printCurrentStats() {
        System.out.println(name + " - " + getStatString());
        System.out.println("Current Health: " + health + "/" + maxHealth);
        printNotifications();
    }

    protected void takeHealthDamage(int damage) {
        health = Math.max(0, health - damage);
        System.out.println(name + "'s health is now " + health + "/" + maxHealth);
    }

    public void receiveDamage(int damage, boolean ignoreArmor) {
        if (ignoreArmor || getTotalArmor() == 0) {
            takeHealthDamage(damage);
        } else {
            double reductionPercent = (getTotalArmor() >= 50) ? 0.10 : 0.05;
            int reducedDamage = (int) Math.round(damage * (1 - reductionPercent));
            baseArmorReduction();
            takeHealthDamage(reducedDamage);
        }
    }

    protected int baseArmorReduction() {
        return 0;
    }

    public boolean addItemToInventory(Item item) {
        try {
            for (int i = 0; i < inventory.length; i++) {
                if (inventory[i] == null) {
                    inventory[i] = item;
                    addNotification(name + " added " + item.getName() + " to inventory. (" + 
                                  item.getBonusType() + " Bonus: " + item.getBonusValue() + ")");
                    return true;
                }
            }
            throw new InventoryFullException(getName() + "'s inventory is full!");
        } catch (InventoryFullException e) {
            addNotification("Error: " + e.getMessage());
            return false;
        }
    }

    public Item removeItemFromInventory(int index) {
        if (index < 0 || index >= inventory.length || inventory[index] == null) {
            return null;
        }
        Item item = inventory[index];
        for (int i = index; i < inventory.length - 1; i++) {
            inventory[i] = inventory[i + 1];
        }
        inventory[inventory.length - 1] = null;
        return item;
    }

    public boolean equipItemFromInventory(int invIndex) {
        if (invIndex < 0 || invIndex >= inventory.length || inventory[invIndex] == null) {
            return false;
        }
        Item item = removeItemFromInventory(invIndex);
        try {
            equipItem(item);
            addNotification(name + " equipped " + item.getName() + " (" + 
                          item.getBonusType() + " Bonus: " + item.getBonusValue() + ").");
            return true;
        } catch (EquipmentFullException e) {
            addNotification("Error: " + e.getMessage());
            addItemToInventory(item);
            return false;
        } catch (BonusNotAllowedException e) {
            addNotification("Error: " + e.getMessage());
            addItemToInventory(item);
            return false;
        }
    }

    public boolean unequipItem(int equipIndex) {
        if (equipIndex < 0 || equipIndex >= equipment.length || equipment[equipIndex] == null) {
            return false;
        }
        Item item = equipment[equipIndex];
        if (addItemToInventory(item)) {
            removeEquipmentAtIndex(equipIndex);
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
            throw new BonusNotAllowedException("Bonus type " + item.getBonusType() + " not allowed for this character!");
        }
        for (int i = 0; i < equipment.length; i++) {
            if (equipment[i] == null) {
                equipment[i] = item;
                addNotification(item.getName() + " equipped successfully.");
                return;
            }
        }
        throw new EquipmentFullException("No equipment slot available!");
    }

    protected abstract boolean isBonusAllowed(String bonusType);

    protected int consumeResource(String resourceType, int cost, int baseResource) {
        int bonus = getEquipmentBonus(resourceType);
        if (bonus >= cost) {
            try {
                consumeResourceBonus(resourceType, cost);
                return baseResource;
            } catch (EquipmentBrokenException e) {
                addNotification(e.getMessage());
                return baseResource;
            }
        } else {
            return baseResource - (cost - bonus);
        }
    }

    protected void consumeResourceBonus(String resourceType, int cost) throws EquipmentBrokenException {
        int remainingCost = cost;
        for (int i = 0; i < equipment.length && remainingCost > 0; i++) {
            if (equipment[i] != null && equipment[i].getBonusType().equals(resourceType)) {
                try {
                    int bonus = equipment[i].getBonusValue();
                    if (bonus >= remainingCost) {
                        equipment[i].reduceBonus(remainingCost);
                        remainingCost = 0;
                    } else {
                        equipment[i].reduceBonus(bonus);
                        remainingCost -= bonus;
                    }
                } catch (EquipmentBrokenException e) {
                    removeEquipmentAtIndex(i);
                    throw e;
                }
            }
        }
    }

    public void removeEquipmentAtIndex(int index) {
        if (index < 0 || index >= equipment.length || equipment[index] == null) {
            return;
        }
        for (int i = index; i < equipment.length - 1; i++) {
            equipment[i] = equipment[i + 1];
        }
        equipment[equipment.length - 1] = null;
    }

    public abstract void attack(Character target);
    public abstract void specialAttack(Character target);

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" - ").append(getStatString()).append("\n");
        sb.append("Equipped Items:\n");
        boolean hasEquipment = false;
        for (Item item : equipment) {
            if (item != null) {
                sb.append("- ").append(item).append("\n");
                hasEquipment = true;
            }
        }
        if (!hasEquipment) {
            sb.append("None\n");
        }
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

    public int getTotalArmor() {
        return baseArmor + getEquipmentBonus("ARMOR");
    }

    public int getTotalBaseArmor() {
        return baseArmor;
    }

    protected boolean isBonusAllowed(String bonusType) {
        return bonusType.equals("ATTACK") || bonusType.equals("ARMOR") || bonusType.equals("ENERGY");
    }

    protected int baseArmorReduction() {
        int oldBase = baseArmor;
        baseArmor = consumeResource("ARMOR", 20, baseArmor);
        return oldBase;
    }

    public void attack(Character target) {
        int requiredEnergy = 10;
        int availableEnergy = energy + getEquipmentBonus("ENERGY");
        
        if (availableEnergy < requiredEnergy) {
            addNotification(name + " performs a weak attack!");
            target.receiveDamage(getAttackPower() / 2, false);
            return;
        }

        energy = consumeResource("ENERGY", requiredEnergy, energy);
        int damage = getAttackPower() + getEquipmentBonus("ATTACK");
        
        addNotification(name + " performs a basic attack on " + target.getName() + 
                      " (Energy reduced by: " + requiredEnergy + ")");
        target.receiveDamage(damage, false);
        printCurrentStats();
    }

    public void specialAttack(Character target) {
        int requiredEnergy = 15;
        int availableEnergy = energy + getEquipmentBonus("ENERGY");
        
        if (availableEnergy < requiredEnergy) {
            addNotification("Not enough energy for special attack! Performing basic attack instead.");
            attack(target);
            return;
        }

        energy = consumeResource("ENERGY", requiredEnergy, energy);
        int damage = getAttackPower() + getEquipmentBonus("ATTACK") + 5;
        
        addNotification(name + " uses Shield Bash on " + target.getName() + 
                      " (Energy reduced by: " + requiredEnergy + ")");
        target.receiveDamage(damage, false);
        printCurrentStats();
    }

    public String getStatString() {
        return "Total Attack: " + (getAttackPower() + getEquipmentBonus("ATTACK")) + 
               ", Total Armor: " + getTotalArmor() + 
               ", Total Energy: " + (energy + getEquipmentBonus("ENERGY"));
    }

    public String toString() {
        return "Warrior " + super.toString();
    }
}

// Mage Class
class Mage extends Character {
    private int mana;

    public Mage(String name, int health, int attackPower, int mana) {
        super(name, health, attackPower, 2, 1);
        this.mana = mana;
    }

    protected boolean isBonusAllowed(String bonusType) {
        return bonusType.equals("ATTACK") || bonusType.equals("MANA");
    }

    public int getTotalArmor() {
        return 0;
    }

    public int getTotalBaseArmor() {
        return 0;
    }

    public void attack(Character target) {
        int requiredMana = 10;
        int availableMana = mana + getEquipmentBonus("MANA");
        
        if (availableMana < requiredMana) {
            addNotification(name + " performs a weak attack!");
            target.receiveDamage(getAttackPower() / 2, false);
            return;
        }

        mana = consumeResource("MANA", requiredMana, mana);
        int damage = getAttackPower() + getEquipmentBonus("ATTACK");
        
        addNotification(name + " casts a basic spell on " + target.getName() + 
                      " (Mana reduced by: " + requiredMana + ")");
        target.receiveDamage(damage, false);
        printCurrentStats();
    }

    public void specialAttack(Character target) {
        int requiredMana = 15;
        int availableMana = mana + getEquipmentBonus("MANA");
        
        if (availableMana < requiredMana) {
            addNotification("Not enough mana for special attack! Performing basic attack instead.");
            attack(target);
            return;
        }

        mana = consumeResource("MANA", requiredMana, mana);
        int damage = getAttackPower() + getEquipmentBonus("ATTACK") + 10;
        
        addNotification(name + " uses a penetrating spell on " + target.getName() + 
                      " (Mana reduced by: " + requiredMana + ")");
        if (target instanceof Warrior) {
            addNotification("This attack ignores armor! Full damage applied.");
            target.receiveDamage(damage, true);
        } else {
            target.receiveDamage(damage, false);
        }
        printCurrentStats();
    }

    public String getStatString() {
        return "Total Attack: " + (getAttackPower() + getEquipmentBonus("ATTACK")) + 
               ", Total Mana: " + (mana + getEquipmentBonus("MANA"));
    }

    public String toString() {
        return "Mage " + super.toString();
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

    protected boolean isBonusAllowed(String bonusType) {
        return bonusType.equals("ATTACK") || bonusType.equals("ACCURACY") || bonusType.equals("ENERGY");
    }

    public int getTotalArmor() {
        return 0;
    }

    public int getTotalBaseArmor() {
        return 0;
    }

    public void attack(Character target) {
        int requiredEnergy = 10;
        int availableEnergy = energy + getEquipmentBonus("ENERGY");
        
        if (availableEnergy < requiredEnergy) {
            addNotification(name + " performs a weak attack!");
            target.receiveDamage(getAttackPower() / 2, false);
            return;
        }

        energy = consumeResource("ENERGY", requiredEnergy, energy);
        int damage = getAttackPower() + getEquipmentBonus("ATTACK");
        
        if (accuracy + getEquipmentBonus("ACCURACY") >= 80) {
            damage += 5;
            addNotification("Precise shot! +5 bonus damage.");
        }
        
        addNotification(name + " fires a basic arrow at " + target.getName() + 
                      " (Energy reduced by: " + requiredEnergy + ")");
        target.receiveDamage(damage, false);
        printCurrentStats();
    }

    public void specialAttack(Character target) {
        int requiredEnergy = 15;
        int availableEnergy = energy + getEquipmentBonus("ENERGY");
        
        if (availableEnergy < requiredEnergy) {
            addNotification("Not enough energy for special attack! Performing basic attack instead.");
            attack(target);
            return;
        }

        energy = consumeResource("ENERGY", requiredEnergy, energy);
        int damage = getAttackPower() + getEquipmentBonus("ATTACK") + 5;
        
        if (accuracy + getEquipmentBonus("ACCURACY") >= 80) {
            damage += 5;
            addNotification("Precise shot! +5 bonus damage.");
        }
        
        addNotification(name + " performs a critical arrow shot on " + target.getName() + 
                      " (Energy reduced by: " + requiredEnergy + ")");
        if (target instanceof Warrior) {
            addNotification("This attack ignores armor! Full damage applied.");
            target.receiveDamage(damage, true);
        } else {
            target.receiveDamage(damage, false);
        }
        printCurrentStats();
    }

    public String getStatString() {
        return "Total Attack: " + (getAttackPower() + getEquipmentBonus("ATTACK")) + 
               ", Total Accuracy: " + (accuracy + getEquipmentBonus("ACCURACY")) + 
               ", Total Energy: " + (energy + getEquipmentBonus("ENERGY"));
    }

    public String toString() {
        return "Archer " + super.toString();
    }
}
public class deneme {
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
        warrior.equipItemFromInventory(0);
        warrior.equipItemFromInventory(0);

        // Mage: "Mana Pendant" (MANA)
        mage.addItemToInventory(new Item("Mana Pendant", "MANA", 8));
        mage.equipItemFromInventory(0);

        //Archers: "Eagle Eye" (ACCURACY), "Energy Booster" (ENERGY)
        archer.addItemToInventory(new Item("Eagle Eye", "ACCURACY", 10));
        archer.addItemToInventory(new Item("Energy Booster", "Energy", 8));
        archer.equipItemFromInventory(0);
        archer.equipItemFromInventory(0);

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
