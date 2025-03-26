// Deniz Buyuksahin
// 20240808701
// 16.03.2025

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
    public String getBonusType() { return bonusType; }
    public int getBonusValue() { return bonusValue; }
    
    @Override
    public String toString() {
        return name + " [" + bonusType + " Bonus: " + bonusValue + "]";
    }
}

class Character {
    protected String name;
    protected int health;
    protected int attackPower;
    protected Item[] inventory;
    protected Item[] equipment;
    protected int bonusHealth = 0;
    protected int bonusMana = 0;
    protected int bonusAccuracy = 0;
    
    public Character(String name, int health, int attackPower, int invSize, int equipSize) {
        this.name = name;
        this.health = health;
        this.attackPower = attackPower;
        this.inventory = new Item[invSize];
        this.equipment = new Item[equipSize];
    }
    
    public void takeDamage(int damage) {
        health = Math.max(0, health - damage);
        System.out.println(name + " takes " + damage + " damage. (Current Health: " + health + ")");
    }
    
    @Override
    public String toString() {
        return name + " [Current Health: " + health + ", Total Attack: " + getTotalAttack() + "]";
    }
    
    public int getTotalAttack() {
        int totalAttack = attackPower;
        for (Item item : equipment) {
            if (item != null && item.getBonusType().equals("ATTACK")) {
                totalAttack += item.getBonusValue();
            }
        }
        return totalAttack;
    }
    
    public void addItemToInventory(Item item) {
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null) {
                inventory[i] = item;
                System.out.println(name + " added " + item.getName() + " to inventory.");
                return;
            }
        }
        System.out.println("Inventory is full!");
    }
    
    public void equipItemFromInventory(int invIndex) {
        if (invIndex >= 0 && invIndex < inventory.length && inventory[invIndex] != null) {
            for (int i = 0; i < equipment.length; i++) {
                if (equipment[i] == null) {
                    equipment[i] = inventory[invIndex];
                    applyItemBonus(equipment[i]);
                    System.out.println(name + " equips " + equipment[i].getName() + " from inventory. (Bonus " + equipment[i].getBonusType() + ": " + equipment[i].getBonusValue() + ")");
                    inventory[invIndex] = null;
                    return;
                }
            }
        }
    }
    
    public void unequipItem(int equipIndex) {
        if (equipIndex >= 0 && equipIndex < equipment.length && equipment[equipIndex] != null) {
            System.out.println(name + " removes " + equipment[equipIndex].getName() + " from equipment. (Bonus " + equipment[equipIndex].getBonusType() + ": 0)");
            equipment[equipIndex] = null;
        }
    }
    
    private void applyItemBonus(Item item) {
        switch (item.getBonusType()) {
            case "ATTACK":
                break;
            case "HEALTH":
                health += item.getBonusValue();
                bonusHealth += item.getBonusValue();
                break;
            case "MANA":
                bonusMana += item.getBonusValue();
                break;
            case "ACCURACY":
                bonusAccuracy += item.getBonusValue();
                break;
        }
    }
}

class Warrior extends Character {
    private double armor;
    
    public Warrior(String name, int health, int attackPower, double armor) {
        super(name, health, attackPower, 6, 3);
        this.armor = armor;
    }
    
    public void warriorAttack(Character target) {
        System.out.println(name + " attacks " + target.name + " with total power " + getTotalAttack());
        target.takeDamage(getTotalAttack());
    }
    
    @Override
    public String toString() {
        return super.toString() + ", Current Armor: " + armor + ", Bonus HEALTH: " + bonusHealth;
    }
}

class Mage extends Character {
    private int mana;
    
    public Mage(String name, int health, int attackPower, int mana) {
        super(name, health, attackPower, 2, 1);
        this.mana = mana;
    }
    
    public void mageAttack(Character target) {
        if (mana >= 10) {
            int damage = getTotalAttack() + 5;
            System.out.println(name + " casts a spell on " + target.name + " dealing " + damage + " damage!");
            target.takeDamage(damage);
            mana -= 10;
            System.out.println("Mage's Remaining Mana: " + mana);
        }
    }
    
    @Override
    public String toString() {
        return super.toString() + ", Current Mana: " + mana + ", Bonus MANA: " + bonusMana;
    }
}

class Archer extends Character {
    private int accuracy;
    
    public Archer(String name, int health, int attackPower, int accuracy) {
        super(name, health, attackPower, 4, 2);
        this.accuracy = accuracy;
    }
    
    public void archerAttack(Character target) {
        int totalAttackPower = getTotalAttack();
        if (accuracy + bonusAccuracy > 80) {
            totalAttackPower += 5;
            System.out.println(name + " lands a critical hit!");
        }
        System.out.println(name + " attacks " + target.name + " with total power " + totalAttackPower);
        target.takeDamage(totalAttackPower);
    }
    
    @Override
    public String toString() {
        return super.toString() + ", Accuracy: " + accuracy + ", Bonus ACCURACY: " + bonusAccuracy;
    }
}
