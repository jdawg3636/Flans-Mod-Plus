package com.flansmod.common.guns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.flansmod.client.model.EnumAnimationType;
import com.flansmod.client.model.EnumMeleeAnimation;
import com.flansmod.client.model.ModelAttachment;
import com.flansmod.client.model.ModelCasing;
import com.flansmod.client.model.ModelFlash;
import net.minecraft.client.model.ModelBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.flansmod.client.model.ModelGun;
import com.flansmod.client.model.ModelMG;
import com.flansmod.common.FlansMod;
import com.flansmod.common.guns.boxes.BlockGunBox;
import com.flansmod.common.paintjob.PaintableType;
import com.flansmod.common.paintjob.Paintjob;
import com.flansmod.common.types.InfoType;
import com.flansmod.common.types.TypeFile;
import com.flansmod.common.vector.Vector3f;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GunType extends PaintableType implements IScope
{
	public static final Random rand = new Random();
	
	//Gun Behaviour Variables
	
	//Recoil Variables
	/** Base value for Upwards cursor/view recoil */
	public float recoilPitch = 0.0F;
	/** Base value for Left/Right cursor/view recoil */
	public float recoilYaw = 0.0F;
	/** Modifier for setting the maximum pitch divergence when randomizing recoil (Recoil 2 + rndRecoil 0.5 == 1.5-2.5 Recoil range) */
	public float rndRecoilPitchRange = 0.5F;
	/** Modifier for setting the maximum yaw divergence when randomizing recoil (Recoil 2 + rndRecoil 0.5 == 1.5-2.5 Recoil range) */
	public float rndRecoilYawRange = 0.3F;
	/** Modifier for decreasing the final pitch recoil while crouching (Recoil 2 + rndRecoil 0.5 + decreaseRecoil 0.5 == 1.0-2.0 Recoil range) */
	public float decreaseRecoilPitch = 0.5F;
	/** Modifier for decreasing the final yaw recoil while crouching (Recoil 2 + rndRecoil 0.5 + decreaseRecoil 0.5 == 1.0-2.0 Recoil range) */
	//This must never be set to 0, will cause massive issues
	public float decreaseRecoilYaw = 0.5F;
	/** Countering gun recoil can be modelled with angle=n^tick where n is the coefficient here. */
	/** HIGHER means less force to center, meaning it takes longer to return. */
	public float recoilCounterCoefficient = 0.8F;
	/** The above variable but for sprinting. */
	public float recoilCounterCoefficientSprinting = 0.9F;
	/** The above variable but for sneaking. */
	public float recoilCounterCoefficientSneaking = 0.7F;
	
	//Ammo & Reload Variables
	/** The list of bullet types that can be used in this gun */
	public List<ShootableType> ammo = new ArrayList<ShootableType>();
	/** Whether the player can press the reload key (default R) to reload this gun */
	public boolean canForceReload = true;
	/** Whether the player can receive ammo for this gun from an ammo mag */
	public boolean allowRearm = true;
	/** The time (in ticks) it takes to reload this gun */
	public int reloadTime;
	/** Number of ammo items that the gun may hold. Most guns will hold one magazine.
	 * Some may hold more, such as Nerf pistols, revolvers or shotguns */
	public int numPrimaryAmmoItems = 1;
	
	//Projectile Mechanic Variables
	/** The amount that bullets spread out when fired from this gun */
	public float bulletSpread;
	public float sneakSpreadMultiplier = 0.63F;
	public float sprintSpreadMultiplier = 1.75F;
	/** If true, spread determined by loaded ammo type */
	public boolean allowSpreadByBullet = false;
	/** Damage inflicted by this gun. Multiplied by the bullet damage. */
	public float damage = 0;
	/** The damage inflicted upon punching someone with this gun */
	public float meleeDamage = 1;
	/** The speed of bullets upon leaving this gun */
	public float bulletSpeed = 5.0F;
	/** The number of bullet entities created by each shot */
	public int numBullets = 1;
	/** Allows you to set how many bullet entities are fired per shot via the ammo used */
	public boolean allowNumBulletsByBulletType = false;
	/** The delay between shots in ticks (1/20ths of seconds) OUTDATED, USE RPM */
	public float shootDelay = 0;
	/** The fire rate of the gun in RPM, 1200 = MAX */
	public float roundsPerMin = 0;
	/** The firing mode of the gun. One of semi-auto, full-auto, minigun or burst */
	public EnumFireMode mode = EnumFireMode.FULLAUTO;
	public EnumFireMode[] submode = new EnumFireMode[]{ EnumFireMode.FULLAUTO };
	public EnumFireMode defaultmode = mode;
	/** The number of bullets to fire per burst in burst mode */
	public int numBurstRounds = 3;
	/** The required speed for minigun mode guns to start firing */
	public float minigunStartSpeed = 15F;
	/** Whether this gun can be used underwater */
	public boolean canShootUnderwater = true;
	/** The secondary function of this gun. By default, the left mouse button triggers this */
	public EnumSecondaryFunction secondaryFunction = EnumSecondaryFunction.ADS_ZOOM;
	public EnumSecondaryFunction secondaryFunctionWhenShoot = null;
	/** If true, then this gun can be dual wielded */
	public boolean oneHanded = false;
	/** For one shot items like a panzerfaust */
	public boolean consumeGunUponUse = false;
	/** Show the crosshair when holding this weapon */
	public boolean showCrosshair = true;
	/** Item to drop on shooting */
	public String dropItemOnShoot = null;
	/** Set these to make guns only usable by a certain type of entity */
	public boolean usableByPlayers = true, usableByMechas = true;
	/** Whether Gun makes players to be EnumAction.bow */
	public EnumAction itemUseAction = EnumAction.bow;
	/** Whether the gun can be hipfired while sprinting */
	/** 0=use flansmod.cfg default, 1=force allow, 2=force deny **/
	public int hipFireWhileSprinting = 0;
	
	//Launcher variables
	public int canLockOnAngle = 5;
	public int lockOnSoundTime = 0;
	public String lockOnSound = "";
	public int maxRangeLockOn = 80;
	public boolean canSetPosition = false;
	/** Determines what the launcher can lock on to */
	public boolean lockOnToPlanes = false, lockOnToVehicles = false, lockOnToMechas = false, lockOnToPlayers = false, lockOnToLivings = false;


	//Shields
	/*A shield is actually a gun without any shoot functionality (similar to knives or binoculars)
	and a load of shield code on top. This means that guns can have in built shields (think Nerf Stampede) */
	/** Whether or not this gun has a shield piece */
	public boolean shield = false;
	/** Shield collision box definition. In model co-ordinates */
	public Vector3f shieldOrigin, shieldDimensions;
	/** Percentage of damage blocked between 0.00-1.00 (0.50F = 50%) */
	public float shieldDamageAbsorption = 0F;

	//Sounds
	/** The sound played upon shooting */
	public String shootSound;
	/** Bullet insert reload sound */
	public String bulletInsert = "defaultshellinsert";
	/** Pump Sound */
	public String actionSound;
	/** The sound to play upon shooting on last round */
	public String lastShootSound;

	/** The sound played upon shooting with a suppressor */
	public String suppressedShootSound;
	/** The length of the sound for looping sounds */
	public int shootSoundLength;
	/** Whether to distort the sound or not. Generally only set to false for looping sounds */
	public String reloadSound;
	/** The sound to play upon reloading when empty */
	public String reloadSoundOnEmpty;
	/** The sound to play open firing when empty(once) */
	public String clickSoundOnEmpty;
	/** The sound to play while holding the weapon in the hand*/
	public String idleSound;
	
	//Sound Modifiers
	/** Whether to distort the sound or not. Generally only set to false for looping sounds */
	public boolean distortSound = true;
	/** The length of the idle sound for looping sounds (miniguns) */
	public int idleSoundLength;
	/** The block range for idle sounds (for miniguns etc) */
	public int idleSoundRange = 50;
	/** The block range for melee sounds  */
	public int meleeSoundRange = 50;
	/** The block range for reload sounds */
	public int reloadSoundRange = 50;
	/** The block range for gunshots sounds  */
	public int gunSoundRange = 50;
	
	/** Sound to be played outside of normal range */
	public String distantShootSound = "";
	/** Max range for the sound to be played */
	public int distantSoundRange = 100;

	//Looping sounds
	/** Whether the looping sounds should be used. Automatically set if the player sets any one of the following sounds */
	public boolean useLoopingSounds = false;
	/** Played when the player starts to hold shoot */
	public String warmupSound;
	public int warmupSoundLength = 20;
	/** Played in a loop until player stops holding shoot */
	public String loopedSound;
	public int loopedSoundLength = 20;
	/** Played when the player stops holding shoot */
	public String cooldownSound;

	//Custom Melee Stuff
	/** The sound to play upon weapon swing */
	public String meleeSound;
	/** The time delay between custom melee attacks */
	public int meleeTime = 1;
	/** The path the melee weapon takes */
	public ArrayList<Vector3f> meleePath = new ArrayList<Vector3f>(), meleePathAngles = new ArrayList<Vector3f>();
	/** The points on the melee weapon that damage is actually done from. */
	public ArrayList<Vector3f> meleeDamagePoints = new ArrayList<Vector3f>();


	//Deployable Settings
	/** If true, then the bullet does not shoot when right clicked, but must instead be placed on the ground */
	public boolean deployable = false;
	/** The deployable model */
	public ModelMG deployableModel;
	/** The deployable model's texture*/
	public String deployableTexture;
	/** Various deployable settings controlling the player view limits and standing position */
	public float standBackDist = 1.5F, topViewLimit = -60F, bottomViewLimit = 30F, sideViewLimit = 45F, pivotHeight = 0.375F;

	//Default Scope Settings. Overriden by scope attachments
	//In many cases, this will simply be iron sights
	/** Default scope overlay texture */
	public String defaultScopeTexture;
	/** Whether the default scope has an overlay */
	public boolean hasScopeOverlay = false;
	/** The zoom level of the default scope */
	public float zoomLevel = 1.0F;
	/** The FOV zoom level of the default scope */
	public float FOVFactor = 1.5F;
	/** Gives night vision while scoped if true */
	public boolean allowNightVision = false;

	//Model variables
	/** For guns with 3D models */
	public ModelGun model;
	/** For making detailed models and scaling down */
	public float modelScale = 1F;
	/** For adding a bullet casing model to render */
	public ModelCasing casingModel;
	/** For adding a muzzle flash model to render */
	public ModelFlash flashModel;
	/** Set a bullet casing texture */
	public String casingTexture;
	/** Set a muzzle flash texture */
	public String flashTexture;
	/** Set a hit marker texture */
	public String hitTexture;

	//Attachment settings
	/** If this is true, then all attachments are allowed. Otherwise the list is checked */
	public boolean allowAllAttachments = false;
	/** The list of allowed attachments for this gun */
	public ArrayList<AttachmentType> allowedAttachments = new ArrayList<AttachmentType>();
	/** Whether each attachment slot is available */
	public boolean allowBarrelAttachments = false, allowScopeAttachments = false,
			allowStockAttachments = false, allowGripAttachments = false, allowGadgetAttachments = false,
			allowSlideAttachments = false, allowPumpAttachments = false, allowAccessoryAttachments = false;
	/** The number of generic attachment slots there are on this gun */
	public int numGenericAttachmentSlots = 0;

	/** The static hashmap of all guns by shortName */
	public static HashMap<String, GunType> guns = new HashMap<String, GunType>();
	/** The static list of all guns */
	public static ArrayList<GunType> gunList = new ArrayList<GunType>();

	//Modifiers
	/** Speeds up or slows down player movement when this item is held */
	public float moveSpeedModifier = 1F;
	/** Gives knockback resistance to the player */
	public float knockbackModifier = 0F;
	/** Default spread of the gun. Do not modify. */
	private float defaultSpread = 0F;

	// Cached gunbox value - so we don't have to search through the gunboxes every single time gui is rendered.
	private String gunBoxName = "";

	public GunType(TypeFile file)
	{
		super(file);
	}

	@Override
	public void preRead(TypeFile file) 
	{	
		super.preRead(file);
	}
	
	@Override
	public void postRead(TypeFile file)
	{
		super.postRead(file);
		
		gunList.add(this);
		guns.put(shortName, this);
	}

	@Override
	protected void read(String[] split, TypeFile file)
	{
		super.read(split, file);
		try
		{
			if(split[0].equals("Damage"))
				damage = Float.parseFloat(split[1]);
			else if(split[0].equals("MeleeDamage"))
			{
				meleeDamage = Float.parseFloat(split[1]);
				if(meleeDamage > 0F)
					secondaryFunction = EnumSecondaryFunction.MELEE;
			}
			else if(split[0].equals("CounterRecoilForce"))
				recoilCounterCoefficient = Float.parseFloat(split[1]);
			else if(split[0].equals("CounterRecoilForceSneaking"))
				recoilCounterCoefficientSneaking = Float.parseFloat(split[1]);
			else if(split[0].equals("CounterRecoilForceSprinting"))
				recoilCounterCoefficientSprinting = Float.parseFloat(split[1]);
			else if(split[0].equals("SneakSpreadModifier"))
				sneakSpreadMultiplier = Float.parseFloat(split[1]);
			else if (split[0].equals("SprintSpreadModifier"))
				sprintSpreadMultiplier = Float.parseFloat(split[1]);
			else if(split[0].equals("CanForceReload"))
				canForceReload = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("AllowRearm"))
				allowRearm = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("ReloadTime"))
				reloadTime = Integer.parseInt(split[1]);
			else if(split[0].equals("Recoil"))
				recoilPitch = Float.parseFloat(split[1]);
			else if(split[0].equals("RecoilYaw"))
				recoilYaw = Float.parseFloat(split[1]) / 10;
			else if(split[0].equals("RandomRecoilRange"))
				rndRecoilPitchRange = Float.parseFloat(split[1]);
			else if(split[0].equals("RandomRecoilYawRange"))
				rndRecoilYawRange = Float.parseFloat(split[1]);
			else if(split[0].equals("DecreaseRecoil"))
				decreaseRecoilPitch = Float.parseFloat(split[1]);
			else if(split[0].equals("DecreaseRecoilYaw"))
				decreaseRecoilYaw = Float.parseFloat(split[1]) > 0 ? Float.parseFloat(split[1]) : 0.5F;
			else if(split[0].equals("Accuracy") || split[0].equals("Spread"))
				defaultSpread = bulletSpread = Float.parseFloat(split[1]);
			else if(split[0].equals("NumBullets"))
				numBullets = Integer.parseInt(split[1]);
			else if(split[0].equals("AllowNumBulletsByBulletType"))
				allowNumBulletsByBulletType = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("AllowSpreadByBullet"))
				allowSpreadByBullet = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("CanLockAngle"))
				canLockOnAngle = Integer.parseInt(split[1]);
			else if(split[0].equals("LockOnSoundTime"))
				lockOnSoundTime = Integer.parseInt(split[1]);
			else if(split[0].equals("LockOnToDriveables"))
				lockOnToPlanes = lockOnToVehicles = lockOnToMechas = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("LockOnToVehicles"))
				lockOnToVehicles = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("LockOnToPlanes"))
				lockOnToPlanes = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("LockOnToMechas"))
				lockOnToMechas = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("LockOnToPlayers"))
				lockOnToPlayers = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("LockOnToLivings"))
				lockOnToLivings = Boolean.parseBoolean(split[1].toLowerCase());	
			
			else if(split[0].equals("ConsumeGunOnUse"))
				consumeGunUponUse = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("ShowCrosshair"))
				showCrosshair = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("DropItemOnShoot"))
				dropItemOnShoot = split[1];
			else if(split[0].equals("NumBurstRounds"))
				numBurstRounds = Integer.parseInt(split[1]);
			else if(split[0].equals("MinigunStartSpeed"))
				minigunStartSpeed = Float.parseFloat(split[1]);
			else if(split[0].equals("ItemUseAction"))
				itemUseAction = EnumAction.valueOf(split[1].toLowerCase());
			else if(split[0].equals("HipFireWhileSprinting"))
				hipFireWhileSprinting = Boolean.parseBoolean(split[1].toLowerCase()) ? 1 : 2;
			else if(split[0].equals("MaxRangeLockOn"))
				maxRangeLockOn = Integer.parseInt(split[1]);


			//Sounds
			else if(split[0].equals("ShootDelay"))
				shootDelay = Float.parseFloat(split[1]);
			else if(split[0].equals("RoundsPerMin"))
				roundsPerMin = Float.parseFloat(split[1]);
			else if(split[0].equals("SoundLength"))
				shootSoundLength = Integer.parseInt(split[1]);
			else if(split[0].equals("DistortSound"))
				distortSound = split[1].equals("True");
			else if(split[0].equals("IdleSoundRange"))
				idleSoundRange = Integer.parseInt(split[1]);
			else if(split[0].equals("MeleeSoundRange"))
				meleeSoundRange = Integer.parseInt(split[1]);
			else if(split[0].equals("ReloadSoundRange"))
				reloadSoundRange = Integer.parseInt(split[1]);
			else if(split[0].equals("GunSoundRange"))
				gunSoundRange = Integer.parseInt(split[1]);
			else if(split[0].equals("ShootSound"))
			{
				shootSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("BulletInsertSound"))
			{
				bulletInsert = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("ActionSound"))
			{
				actionSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("LastShootSound"))
			{
				lastShootSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("SuppressedShootSound"))
			{
				suppressedShootSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("ReloadSound"))
			{
				reloadSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("EmptyReloadSound"))
			{
				reloadSoundOnEmpty = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("EmptyClickSound"))
			{
				clickSoundOnEmpty = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("IdleSound"))
			{
				idleSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("IdleSoundLength"))
				idleSoundLength = Integer.parseInt(split[1]);
			else if(split[0].equals("MeleeSound"))
			{
				meleeSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}

			//Looping sounds
			else if(split[0].equals("WarmupSound"))
			{
				warmupSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("WarmupSoundLength"))
				warmupSoundLength = Integer.parseInt(split[1]);
			else if(split[0].equals("LoopedSound") || split[0].equals("SpinSound"))
			{
				loopedSound = split[1];
				useLoopingSounds = true;
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("LoopedSoundLength") || split[0].equals("SpinSoundLength"))
				loopedSoundLength = Integer.parseInt(split[1]);
			else if(split[0].equals("CooldownSound"))
			{
				cooldownSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			}
			else if(split[0].equals("LockOnSound"))
			{
				lockOnSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			} else if(split[0].equals("DistantSound")) {
				distantShootSound = split[1];
				FlansMod.proxy.loadSound(contentPack, "guns", split[1]);
			} else if (split[0].equals("DistantSoundRange")) {
				distantSoundRange = Integer.parseInt(split[1]);
			}

			//Modes and zoom settings
			else if(split[0].equals("Mode"))
			{
				mode = EnumFireMode.getFireMode(split[1]);
				defaultmode = mode;
				submode = new EnumFireMode[ split.length - 1 ];
				for(int i=0; i<submode.length; i++)
				{
					submode[i] = EnumFireMode.getFireMode(split[1 + i]);
				}
			}
			else if(split[0].equals("Scope"))
			{
				hasScopeOverlay = true;
				if (split[1].equals("None"))
					hasScopeOverlay = false;
				else defaultScopeTexture = split[1];
			}
			else if(split[0].equals("AllowNightVision"))
			{
				allowNightVision = Boolean.parseBoolean(split[1]);
			}
			else if(split[0].equals("ZoomLevel"))
			{
				zoomLevel = Float.parseFloat(split[1]);
				if(zoomLevel > 1F)
					secondaryFunction = EnumSecondaryFunction.ZOOM;
			}
			else if(split[0].equals("FOVZoomLevel"))
			{
				FOVFactor = Float.parseFloat(split[1]);
				if(FOVFactor > 1F)
					secondaryFunction = EnumSecondaryFunction.ADS_ZOOM;
			}
			else if(split[0].equals("Deployable"))
				deployable = split[1].equals("True");
			else if(FMLCommonHandler.instance().getSide().isClient() && deployable && split[0].equals("DeployedModel"))
				deployableModel = FlansMod.proxy.loadModel(split[1], shortName, ModelMG.class);
			else if(FMLCommonHandler.instance().getSide().isClient() && (split[0].equals("Model")))
				model = FlansMod.proxy.loadModel(split[1], shortName, ModelGun.class);
			else if(FMLCommonHandler.instance().getSide().isClient() && (split[0].equals("CasingModel")))
				casingModel = FlansMod.proxy.loadModel(split[1], shortName, ModelCasing.class);
			else if(FMLCommonHandler.instance().getSide().isClient() && (split[0].equals("FlashModel")))
				flashModel = FlansMod.proxy.loadModel(split[1], shortName, ModelFlash.class);
			else if(split[0].equals("CasingTexture"))
				casingTexture = split[1];
			else if(split[0].equals("FlashTexture"))
				flashTexture = split[1];
			else if(split[0].equals("ModelScale"))
				modelScale = Float.parseFloat(split[1]);
			else if(split[0].equals("Texture"))
				texture = split[1];
			else if(split[0].equals("HitTexture"))
				hitTexture = split[1];
			else if(split[0].equals("DeployedTexture"))
				deployableTexture = split[1];
			else if(split[0].equals("StandBackDistance"))
				standBackDist = Float.parseFloat(split[1]);
			else if(split[0].equals("TopViewLimit"))
				topViewLimit = -Float.parseFloat(split[1]);
			else if(split[0].equals("BottomViewLimit"))
				bottomViewLimit = Float.parseFloat(split[1]);
			else if(split[0].equals("SideViewLimit"))
				sideViewLimit = Float.parseFloat(split[1]);
			else if(split[0].equals("PivotHeight"))
				pivotHeight = Float.parseFloat(split[1]);
			else if(split[0].equals("Ammo"))
			{
				ShootableType type = ShootableType.getShootableType(split[1]);
				if(type != null)
					ammo.add(type);
			}
			else if(split[0].equals("NumAmmoSlots") || split[0].equals("NumAmmoItemsInGun") || split[0].equals("LoadIntoGun"))
				numPrimaryAmmoItems = Integer.parseInt(split[1]);
			else if(split[0].equals("BulletSpeed"))
				bulletSpeed = Float.parseFloat(split[1]);
			else if(split[0].equals("CanShootUnderwater"))
				canShootUnderwater = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("CanSetPosition"))
				canSetPosition = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("OneHanded"))
				oneHanded = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("SecondaryFunction"))
				secondaryFunction = EnumSecondaryFunction.get(split[1]);
			else if(split[0].equals("UsableByPlayers"))
				usableByPlayers = Boolean.parseBoolean(split[1]);
			else if(split[0].equals("UsableByMechas"))
				usableByMechas = Boolean.parseBoolean(split[1]);

			//Custom Melee Stuff
			else if(split[0].equals("UseCustomMelee") && Boolean.parseBoolean(split[1]))
				secondaryFunction = EnumSecondaryFunction.CUSTOM_MELEE;
			else if(split[0].equals("UseCustomMeleeWhenShoot") && Boolean.parseBoolean(split[1]))
				secondaryFunctionWhenShoot = EnumSecondaryFunction.CUSTOM_MELEE;
			else if(split[0].equals("MeleeTime"))
				meleeTime = Integer.parseInt(split[1]);
			else if(split[0].equals("AddNode"))
			{
				meleePath.add(new Vector3f(Float.parseFloat(split[1]) / 16F, Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F));
				meleePathAngles.add(new Vector3f(Float.parseFloat(split[4]), Float.parseFloat(split[5]), Float.parseFloat(split[6])));
			}
			else if(split[0].equals("MeleeDamagePoint") || split[0].equals("MeleeDamageOffset"))
			{
				meleeDamagePoints.add(new Vector3f(Float.parseFloat(split[1]) / 16F, Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F));
			}

			//Player modifiers
			else if(split[0].equals("MoveSpeedModifier") || split[0].equals("Slowness"))
				moveSpeedModifier = Float.parseFloat(split[1]);
			else if(split[0].equals("KnockbackReduction") || split[0].equals("KnockbackModifier"))
				knockbackModifier = Float.parseFloat(split[1]);

			//Attachment settings
			else if(split[0].equals("AllowAllAttachments"))
				allowAllAttachments = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("AllowAttachments"))
			{
				for(int i = 1; i < split.length; i++)
				{
					allowedAttachments.add(AttachmentType.getAttachment(split[i]));
				}
			}
			else if(split[0].equals("AllowBarrelAttachments"))
				allowBarrelAttachments = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("AllowScopeAttachments"))
				allowScopeAttachments = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("AllowStockAttachments"))
				allowStockAttachments = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("AllowGripAttachments"))
				allowGripAttachments = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("AllowGadgetAttachments"))
				allowGadgetAttachments = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("AllowSlideAttachments"))
				allowSlideAttachments = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("AllowPumpAttachments"))
				allowPumpAttachments = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("AllowAccessoryAttachments"))
				allowAccessoryAttachments = Boolean.parseBoolean(split[1].toLowerCase());
			else if(split[0].equals("NumGenericAttachmentSlots"))
				numGenericAttachmentSlots = Integer.parseInt(split[1]);

			//Shield settings
			else if(split[0].toLowerCase().equals("shield"))
			{
				shield = true;
				shieldDamageAbsorption = Float.parseFloat(split[1]);
				shieldOrigin = new Vector3f(Float.parseFloat(split[2]) / 16F, Float.parseFloat(split[3]) / 16F, Float.parseFloat(split[4]) / 16F);
				shieldDimensions = new Vector3f(Float.parseFloat(split[5]) / 16F, Float.parseFloat(split[6]) / 16F, Float.parseFloat(split[7]) / 16F);
			}

			else if(FMLCommonHandler.instance().getSide().isClient()) {
				processAnimationConfigs(split);
			}

		}
		catch (Exception e)
		{
			if(split!=null)
			{
				String msg = " : ";
				for(String s : split) msg = msg +" "+ s;
				FlansMod.log("Reading gun file failed. " + file.name + msg);
			}
			else
			{
				FlansMod.log("Reading gun file failed. " + file.name);
			}
			if(FlansMod.printStackTrace)
			{
				e.printStackTrace();
			}
		}


	}

	public void processAnimationConfigs(String[] split) {
		if (split[0].equals("animMinigunBarrelOrigin"))
			model.minigunBarrelOrigin = parseVector3f(split);
		else if (split[0].equals("animBarrelAttachPoint"))
			model.barrelAttachPoint = parseVector3f(split);
		else if (split[0].equals("animBarrelAttachPoint"))
			model.barrelAttachPoint = parseVector3f(split);
		else if (split[0].equals("animScopeAttachPoint"))
			model.scopeAttachPoint = parseVector3f(split);
		else if (split[0].equals("animStockAttachPoint"))
			model.stockAttachPoint = parseVector3f(split);
		else if (split[0].equals("animGripAttachPoint"))
			model.gripAttachPoint = parseVector3f(split);
		else if (split[0].equals("animGadgetAttachPoint"))
			model.gadgetAttachPoint = parseVector3f(split);
		else if (split[0].equals("animSlideAttachPoint"))
			model.slideAttachPoint = parseVector3f(split);
		else if (split[0].equals("animPumpAttachPoint"))
			model.pumpAttachPoint = parseVector3f(split);
		else if (split[0].equals("animAccessoryAttachPoint"))
			model.accessoryAttachPoint = parseVector3f(split);
		
		else if (split[0].equals("animDefaultBarrelFlashPoint"))
			model.defaultBarrelFlashPoint = parseVector3f(split);
		else if (split[0].equals("animMuzzleFlashPoint"))
			model.muzzleFlashPoint = parseVector3f(split);
		
		else if (split[0].equals("animHasFlash"))
			model.hasFlash = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animHasArms"))
			model.hasArms = Boolean.parseBoolean(split[1]);

		else if (split[0].equals("animLeftArmPos"))
			model.leftArmPos = parseVector3f(split);
		else if (split[0].equals("animLeftArmRot"))
			model.leftArmRot = parseVector3f(split);
		else if (split[0].equals("animLeftArmScale"))
			model.leftArmScale = parseVector3f(split);
		else if (split[0].equals("animRightArmPos"))
			model.rightArmPos = parseVector3f(split);
		else if (split[0].equals("animRightArmRot"))
			model.rightArmRot = parseVector3f(split);
		else if (split[0].equals("animRightArmScale"))
			model.rightArmScale = parseVector3f(split);
		
		else if (split[0].equals("animRightArmReloadPos"))
			model.rightArmReloadPos = parseVector3f(split);
		else if (split[0].equals("animRightArmReloadRot"))
			model.rightArmReloadRot = parseVector3f(split);
		else if (split[0].equals("animLeftArmReloadPos"))
			model.leftArmReloadPos = parseVector3f(split);
		else if (split[0].equals("animLeftArmReloadRot"))
			model.leftArmReloadRot = parseVector3f(split);

		else if (split[0].equals("animRightArmChargePos"))
			model.rightArmChargePos = parseVector3f(split);
		else if (split[0].equals("animRightArmChargeRot"))
			model.rightArmChargeRot = parseVector3f(split);
		else if (split[0].equals("animLeftArmChargePos"))
			model.leftArmChargePos = parseVector3f(split);
		else if (split[0].equals("animLeftArmChargeRot"))
			model.leftArmChargeRot = parseVector3f(split);

		else if (split[0].equals("animStagedRightArmReloadPos"))
			model.stagedrightArmReloadPos = parseVector3f(split);
		else if (split[0].equals("animStagedRightArmReloadRot"))
			model.stagedrightArmReloadRot = parseVector3f(split);
		else if (split[0].equals("animStagedLeftArmReloadPos"))
			model.stagedleftArmReloadPos = parseVector3f(split);
		else if (split[0].equals("animStagedLeftArmReloadRot"))
			model.stagedleftArmReloadRot = parseVector3f(split);

		else if (split[0].equals("animRightHandAmmo"))
			model.rightHandAmmo = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animLeftHandAmmo"))
			model.leftHandAmmo = Boolean.parseBoolean(split[1]);
		
		else if (split[0].equals("animGunSlideDistance"))
			model.gunSlideDistance = Float.parseFloat(split[1]);
		else if (split[0].equals("animAltGunSlideDistance"))
			model.altgunSlideDistance = Float.parseFloat(split[1]);
		else if (split[0].equals("animRecoilSlideDistance"))
			model.RecoilSlideDistance = Float.parseFloat(split[1]);
		else if (split[0].equals("animRotatedSlideDistance"))
			model.RotateSlideDistance = Float.parseFloat(split[1]);
		else if (split[0].equals("animShakeDistance"))
			model.ShakeDistance = Float.parseFloat(split[1]);
		else if (split[0].equals("animRecoilAmount"))
			model.recoilAmount = Float.parseFloat(split[1]);
		
		else if (split[0].equals("animCasingAnimDistance"))
			model.casingAnimDistance = parseVector3f(split);
		else if (split[0].equals("animCasingAnimSpread"))
			model.casingAnimSpread = parseVector3f(split);
		else if (split[0].equals("animCasingAnimTime"))
			model.casingAnimTime = Integer.parseInt(split[1]);
		else if (split[0].equals("animCasingRotateVector"))
			model.casingRotateVector = parseVector3f(split);
		else if (split[0].equals("animCasingAttachPoint"))
			model.casingAttachPoint = parseVector3f(split);
		else if (split[0].equals("animCasingDelay"))
			model.casingDelay = Integer.parseInt(split[1]);
		else if (split[0].equals("animCasingScale"))
			model.caseScale = Float.parseFloat(split[1]);
		else if (split[0].equals("animFlashScale"))
			model.flashScale = Float.parseFloat(split[1]);
		
		else if (split[0].equals("animChargeHandleDistance"))
			model.chargeHandleDistance = Float.parseFloat(split[1]);
		else if (split[0].equals("animChargeDelay"))
			model.chargeDelay = Integer.parseInt(split[1]);
		else if (split[0].equals("animChargeDelayAfterReload"))
			model.chargeDelayAfterReload = Integer.parseInt(split[1]);
		else if (split[0].equals("animChargeTime"))
			model.chargeTime = Integer.parseInt(split[1]);
		else if (split[0].equals("animCountOnRightHandSide"))
			model.countOnRightHandSide = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animIsBulletCounterActive"))
			model.isBulletCounterActive = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animIsAdvBulletCounterActive"))
			model.isAdvBulletCounterActive = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animAnimationType")) {
			if (split[1].equals("NONE"))
				model.animationType = EnumAnimationType.NONE;
			else if (split[1].equals("BOTTOM_CLIP"))
				model.animationType = EnumAnimationType.BOTTOM_CLIP;
			else if (split[1].equals("CUSTOMBOTTOM_CLIP"))
				model.animationType = EnumAnimationType.CUSTOMBOTTOM_CLIP;
			else if (split[1].equals("PISTOL_CLIP"))
				model.animationType = EnumAnimationType.PISTOL_CLIP;
			else if (split[1].equals("CUSTOMPISTOL_CLIP"))
				model.animationType = EnumAnimationType.CUSTOMPISTOL_CLIP;
			else if (split[1].equals("TOP_CLIP"))
				model.animationType = EnumAnimationType.TOP_CLIP;
			else if (split[1].equals("CUSTOMTOP_CLIP"))
				model.animationType = EnumAnimationType.CUSTOMTOP_CLIP;
			else if (split[1].equals("SIDE_CLIP"))
				model.animationType = EnumAnimationType.SIDE_CLIP;
			else if (split[1].equals("CUSTOMSIDE_CLIP"))
				model.animationType = EnumAnimationType.CUSTOMSIDE_CLIP;
			else if (split[1].equals("P90"))
				model.animationType = EnumAnimationType.P90;
			else if (split[1].equals("CUSTOMP90"))
				model.animationType = EnumAnimationType.CUSTOMP90;
			else if (split[1].equals("SHOTGUN"))
				model.animationType = EnumAnimationType.SHOTGUN;
			else if (split[1].equals("CUSTOMSHOTGUN"))
				model.animationType = EnumAnimationType.CUSTOMSHOTGUN;
			else if (split[1].equals("RIFLE"))
				model.animationType = EnumAnimationType.RIFLE;
			else if (split[1].equals("CUSTOMRIFLE"))
				model.animationType = EnumAnimationType.CUSTOMRIFLE;
			else if (split[1].equals("REVOLVER"))
				model.animationType = EnumAnimationType.REVOLVER;
			else if (split[1].equals("CUSTOMREVOLVER"))
				model.animationType = EnumAnimationType.CUSTOMREVOLVER;
			else if (split[1].equals("REVOLVER2"))
				model.animationType = EnumAnimationType.REVOLVER;
			else if (split[1].equals("CUSTOMREVOLVER2"))
				model.animationType = EnumAnimationType.CUSTOMREVOLVER;
			else if (split[1].equals("END_LOADED"))
				model.animationType = EnumAnimationType.END_LOADED;
			else if (split[1].equals("CUSTOMEND_LOADED"))
				model.animationType = EnumAnimationType.CUSTOMEND_LOADED;
			else if (split[1].equals("RIFLE_TOP"))
				model.animationType = EnumAnimationType.RIFLE_TOP;
			else if (split[1].equals("CUSTOMRIFLE_TOP"))
				model.animationType = EnumAnimationType.CUSTOMRIFLE_TOP;
			else if (split[1].equals("BULLPUP"))
				model.animationType = EnumAnimationType.BULLPUP;
			else if (split[1].equals("CUSTOMBULLPUP"))
				model.animationType = EnumAnimationType.CUSTOMBULLPUP;
			else if (split[1].equals("ALT_PISTOL_CLIP"))
				model.animationType = EnumAnimationType.ALT_PISTOL_CLIP;
			else if (split[1].equals("CUSTOMALT_PISTOL_CLIP"))
				model.animationType = EnumAnimationType.CUSTOMALT_PISTOL_CLIP;
			else if (split[1].equals("GENERIC"))
				model.animationType = EnumAnimationType.GENERIC;
			else if (split[1].equals("CUSTOMGENERIC"))
				model.animationType = EnumAnimationType.CUSTOMGENERIC;
			else if (split[1].equals("BACK_LOADED"))
				model.animationType = EnumAnimationType.BACK_LOADED;
			else if (split[1].equals("CUSTOMBACK_LOADED"))
				model.animationType = EnumAnimationType.CUSTOMBACK_LOADED;
			else if (split[1].equals("STRIKER"))
				model.animationType = EnumAnimationType.STRIKER;
			else if (split[1].equals("CUSTOMSTRIKER"))
				model.animationType = EnumAnimationType.CUSTOMSTRIKER;
			else if (split[1].equals("BREAK_ACTION"))
				model.animationType = EnumAnimationType.BREAK_ACTION;
			else if (split[1].equals("CUSTOMBREAK_ACTION"))
				model.animationType = EnumAnimationType.CUSTOMBREAK_ACTION;
			else if (split[1].equals("CUSTOM"))
				model.animationType = EnumAnimationType.CUSTOM;
		}
		else if (split[0].equals("animMeleeAnimation")) {
			if (split[1].equals("DEFAULT"))
				model.meleeAnimation = EnumMeleeAnimation.DEFAULT;
			else if (split[1].equals("NONE"))
				model.meleeAnimation = EnumMeleeAnimation.NONE;
			else if (split[1].equals("BLUNT_SWING"))
				model.meleeAnimation = EnumMeleeAnimation.BLUNT_SWING;
			else if (split[1].equals("BLUNT_BASH"))
				model.meleeAnimation = EnumMeleeAnimation.BLUNT_BASH;
			else if (split[1].equals("STAB_UNDERARM"))
				model.meleeAnimation = EnumMeleeAnimation.STAB_UNDERARM;
			else if (split[1].equals("STAB_OVERARM"))
				model.meleeAnimation = EnumMeleeAnimation.STAB_OVERARM;
		}
		else if (split[0].equals("animTiltGunTime"))
			model.tiltGunTime = Float.parseFloat(split[1]);
		else if (split[0].equals("animUnloadClipTime"))
			model.unloadClipTime = Float.parseFloat(split[1]);
		else if (split[0].equals("animLoadClipTime"))
			model.loadClipTime = Float.parseFloat(split[1]);

		else if (split[0].equals("animScopeIsOnSlide"))
			model.scopeIsOnSlide = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animScopeIsOnBreakAction"))
			model.scopeIsOnBreakAction = Boolean.parseBoolean(split[1]);

		else if (split[0].equals("animNumBulletsInReloadAnimation"))
			model.numBulletsInReloadAnimation = Float.parseFloat(split[1]);

		else if (split[0].equals("animPumpDelay"))
			model.pumpDelay = Integer.parseInt(split[1]);
		else if (split[0].equals("animPumpDelayAfterReload"))
			model.pumpDelayAfterReload = Integer.parseInt(split[1]);
		else if (split[0].equals("animPumpTime"))
			model.pumpTime = Integer.parseInt(split[1]);
		else if (split[0].equals("animHammerDelay"))
			model.hammerDelay = Integer.parseInt(split[1]);

		else if (split[0].equals("animPumpHandleDistance"))
			model.pumpHandleDistance = Float.parseFloat(split[1]);
		else if (split[0].equals("animEndLoadedAmmoDistance"))
			model.endLoadedAmmoDistance = Float.parseFloat(split[1]);
		else if (split[0].equals("animBreakActionAmmoDistance"))
			model.breakActionAmmoDistance = Float.parseFloat(split[1]);
		else if (split[0].equals("animScopeIsOnBreakAction"))
			model.scopeIsOnBreakAction = Boolean.parseBoolean(split[1]);

		else if (split[0].equals("animGripIsOnPump"))
			model.gripIsOnPump = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animGadgetsOnPump"))
			model.gripIsOnPump = Boolean.parseBoolean(split[1]);
		
		else if (split[0].equals("animBarrelBreakPoint"))
			model.barrelBreakPoint = parseVector3f(split);
		else if (split[0].equals("animAltBarrelBreakPoint"))
			model.altbarrelBreakPoint = parseVector3f(split);

		else if (split[0].equals("animRevolverFlipAngle"))
			model.revolverFlipAngle = Float.parseFloat(split[1]);
		else if (split[0].equals("animRevolver2FlipAngle"))
			model.revolver2FlipAngle = Float.parseFloat(split[1]);
		
		else if (split[0].equals("animRevolverFlipPoint"))
			model.revolverFlipPoint = parseVector3f(split);
		else if (split[0].equals("animRevolver2FlipPoint"))
			model.revolver2FlipPoint = parseVector3f(split);
		
		else if (split[0].equals("animBreakAngle"))
			model.breakAngle = Float.parseFloat(split[1]);
		else if (split[0].equals("animAltBreakAngle"))
			model.altbreakAngle = Float.parseFloat(split[1]);
		
		else if (split[0].equals("animSpinningCocking"))
			model.spinningCocking = Boolean.parseBoolean(split[1]);

		else if (split[0].equals("animSpinPoint"))
			model.spinPoint = parseVector3f(split);
		else if (split[0].equals("animHammerSpinPoint"))
			model.hammerSpinPoint = parseVector3f(split);
		else if (split[0].equals("animAltHammerSpinPoint"))
			model.althammerSpinPoint = parseVector3f(split);
		else if (split[0].equals("animHammerAngle"))
			model.hammerAngle = Float.parseFloat(split[1]);
		else if (split[0].equals("animAltHammerAngle"))
			model.althammerAngle = Float.parseFloat(split[1]);

		else if (split[0].equals("animIsSingleAction"))
			model.isSingleAction = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animSlideLockOnEmpty"))
			model.slideLockOnEmpty = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animLeftHandPump"))
			model.lefthandPump = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animRightHandPump"))
			model.righthandPump = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animLeftHandCharge"))
			model.leftHandCharge = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animRightHandCharge"))
			model.rightHandCharge = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animLeftHandBolt"))
			model.leftHandBolt = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animRightHandBolt"))
			model.rightHandBolt = Boolean.parseBoolean(split[1]);
		
		else if (split[0].equals("animPumpModifier"))
			model.pumpModifier = Float.parseFloat(split[1]);
		else if (split[0].equals("animChargeModifier"))
			model.chargeModifier = parseVector3f(split);
		else if (split[0].equals("animGunOffset"))
			model.gunOffset = Float.parseFloat(split[1]);
		else if (split[0].equals("animCrouchZoom"))
			model.crouchZoom = Float.parseFloat(split[1]);
		else if (split[0].equals("animFancyStance"))
			model.fancyStance = Boolean.parseBoolean(split[1]);
		else if (split[0].equals("animStanceTranslate"))
			model.stanceTranslate = parseVector3f(split);
		else if (split[0].equals("animStanceRotate"))
			model.stanceRotate = parseVector3f(split);

		else if (split[0].equals("animRotateGunVertical"))
			model.rotateGunVertical= Float.parseFloat(split[1]);
		else if (split[0].equals("animRotateGunHorizontal"))
			model.rotateGunHorizontal = Float.parseFloat(split[1]);
		else if (split[0].equals("animTiltGun"))
			model.tiltGun = Float.parseFloat(split[1]);
		else if (split[0].equals("animTranslateGun"))
			model.translateGun = parseVector3f(split);
		else if (split[0].equals("animRotateClipVertical"))
			model.rotateClipVertical = Float.parseFloat(split[1]);
		else if (split[0].equals("animStagedRotateClipVertical"))
			model.stagedrotateClipVertical= Float.parseFloat(split[1]);
		else if (split[0].equals("animRotateClipHorizontal"))
			model.rotateClipVertical = Float.parseFloat(split[1]);
		else if (split[0].equals("animStagedRotateClipHorizontal"))
			model.stagedrotateClipVertical = Float.parseFloat(split[1]);
		else if (split[0].equals("animTiltClip"))
			model.tiltClip = Float.parseFloat(split[1]);
		else if (split[0].equals("animStagedTiltClip"))
			model.stagedtiltClip = Float.parseFloat(split[1]);
		else if (split[0].equals("animTranslateClip"))
			model.translateClip = parseVector3f(split);
		else if (split[0].equals("animStagedTranslateClip"))
			model.stagedtranslateClip = parseVector3f(split);
		else if (split[0].equals("animStagedReload"))
			model.stagedReload = Boolean.parseBoolean(split[1]);

		else if (split[0].equals("animThirdPersonOffset"))
			model.thirdPersonOffset = parseVector3f(split);
		else if (split[0].equals("animItemFrameOffset"))
			model.itemFrameOffset = parseVector3f(split);
	}

	/** Used only for driveables */
	public boolean isAmmo(ShootableType type)
	{
		return ammo.contains(type);
	}

	public boolean isAmmo(ShootableType type, ItemStack stack)
	{
		boolean result = ammo.contains(type);

		if(getGrip(stack) != null && getSecondaryFire(stack))
		{
			List<ShootableType> t = new ArrayList<ShootableType>();
			for(String s: getGrip(stack).secondaryAmmo)
			{
				ShootableType shoot = ShootableType.getShootableType(s);
				if(type != null)
					t.add(shoot);
			}
			result = t.contains(type);
		}

		return result;
	}

	public Vector3f parseVector3f(String[] inp) {
		return new Vector3f(Float.parseFloat(inp[1]), Float.parseFloat(inp[2]), Float.parseFloat(inp[3]));
	}

	public boolean isAmmo(ItemStack stack)
	{
		if (stack == null)
			return false;
		else if(stack.getItem() instanceof ItemBullet)
			{
			return isAmmo(((ItemBullet)stack.getItem()).type, stack);
		}
		else if(stack.getItem() instanceof ItemGrenade)
		{
			return isAmmo(((ItemGrenade)stack.getItem()).type, stack);
		}
		return false;
	}

	/** To be overriden by subtypes for model reloading */
	public void reloadModel()
	{
		model = FlansMod.proxy.loadModel(modelString, shortName, ModelGun.class);
	}

	@Override
	public float getZoomFactor()
	{
		return zoomLevel;
	}

	@Override
	public boolean hasZoomOverlay()
	{
		return hasScopeOverlay;
	}

	@Override
	public String getZoomOverlay()
	{
		return defaultScopeTexture;
	}

	@Override
	public float getFOVFactor()
	{
		return FOVFactor;
	}

	//ItemStack specific methods

	/** Return the currently active scope on this gun. Search attachments, and by default, simply give the gun */
	public IScope getCurrentScope(ItemStack gunStack)
	{
		IScope attachedScope = getScope(gunStack);
		return attachedScope == null ? this : attachedScope;
	}

	/** Returns all attachments currently attached to the specified gun */
	public ArrayList<AttachmentType> getCurrentAttachments(ItemStack gun)
	{
		checkForTags(gun);
		ArrayList<AttachmentType> attachments = new ArrayList<AttachmentType>();
		NBTTagCompound attachmentTags = gun.stackTagCompound.getCompoundTag("attachments");
		NBTTagList genericsList = attachmentTags.getTagList("generics", (byte)10); //TODO : Check this 10 is correct
		for(int i = 0; i < numGenericAttachmentSlots; i++)
		{
			appendToList(gun, "generic_" + i, attachments);
		}
		appendToList(gun, "barrel", attachments);
		appendToList(gun, "scope", attachments);
		appendToList(gun, "stock", attachments);
		appendToList(gun, "grip", attachments);
		appendToList(gun, "gadget", attachments);
		appendToList(gun, "slide", attachments);
		appendToList(gun, "pump", attachments);
		appendToList(gun, "accessory", attachments);
		return attachments;
	}

	/** Private method for attaching attachments to a list of attachments with a nullcheck */
	private void appendToList(ItemStack gun, String name, ArrayList<AttachmentType> attachments)
	{
		AttachmentType type = getAttachment(gun, name);
		if(type != null) attachments.add(type);
	}

	//Attachment getter methods
	public AttachmentType getBarrel(ItemStack gun) { return getAttachment(gun, "barrel"); }
	public AttachmentType getScope(ItemStack gun) { return getAttachment(gun, "scope"); }
	public AttachmentType getStock(ItemStack gun) { return getAttachment(gun, "stock"); }
	public AttachmentType getGrip(ItemStack gun) { return getAttachment(gun, "grip"); }
	public AttachmentType getGadget(ItemStack gun) { return getAttachment(gun, "gadget"); }
	public AttachmentType getSlide(ItemStack gun) { return getAttachment(gun, "slide"); }
	public AttachmentType getPump(ItemStack gun) { return getAttachment(gun, "pump"); }
	public AttachmentType getAccessory(ItemStack gun) { return getAttachment(gun, "accessory"); }
	public AttachmentType getGeneric(ItemStack gun, int i) { return getAttachment(gun, "generic_" + i); }

	//Attachment ItemStack getter methods
	public ItemStack getBarrelItemStack(ItemStack gun) { return getAttachmentItemStack(gun, "barrel"); }
	public ItemStack getScopeItemStack(ItemStack gun) { return getAttachmentItemStack(gun, "scope"); }
	public ItemStack getStockItemStack(ItemStack gun) { return getAttachmentItemStack(gun, "stock"); }
	public ItemStack getGripItemStack(ItemStack gun) { return getAttachmentItemStack(gun, "grip"); }
	public ItemStack getGadgetItemStack(ItemStack gun) { return getAttachmentItemStack(gun, "gadget"); }
	public ItemStack getSlideItemStack(ItemStack gun) { return getAttachmentItemStack(gun, "slide"); }
	public ItemStack getPumpItemStack(ItemStack gun) { return getAttachmentItemStack(gun, "pump"); }
	public ItemStack getAccessoryItemStack(ItemStack gun) { return getAttachmentItemStack(gun, "accessory"); }
	public ItemStack getGenericItemStack(ItemStack gun, int i) { return getAttachmentItemStack(gun, "generic_" + i); }

	/** Generalised attachment getter method */
	public AttachmentType getAttachment(ItemStack gun, String name)
	{
		checkForTags(gun);
		return AttachmentType.getFromNBT(gun.stackTagCompound.getCompoundTag("attachments").getCompoundTag(name));
	}

	/** Generalised attachment ItemStack getter method */
	public ItemStack getAttachmentItemStack(ItemStack gun, String name)
	{
		checkForTags(gun);
		return ItemStack.loadItemStackFromNBT(gun.stackTagCompound.getCompoundTag("attachments").getCompoundTag(name));
	}

	/** Method to check for null tags and assign default empty tags in that case */
	public void checkForTags(ItemStack gun)
	{
		//If the gun has no tags, give it some
		if(!gun.hasTagCompound())
		{
			gun.stackTagCompound = new NBTTagCompound();
		}
		//If the gun has no attachment tags, give it some
		if(!gun.stackTagCompound.hasKey("attachments"))
		{
			NBTTagCompound attachmentTags = new NBTTagCompound();
			for(int i = 0; i < numGenericAttachmentSlots; i++)
				attachmentTags.setTag("generic_" + i, new NBTTagCompound());
			attachmentTags.setTag("barrel", new NBTTagCompound());
			attachmentTags.setTag("scope", new NBTTagCompound());
			attachmentTags.setTag("stock", new NBTTagCompound());
			attachmentTags.setTag("grip", new NBTTagCompound());
			attachmentTags.setTag("gadget", new NBTTagCompound());
			attachmentTags.setTag("slide", new NBTTagCompound());
			attachmentTags.setTag("pump", new NBTTagCompound());
			attachmentTags.setTag("accessory", new NBTTagCompound());

			gun.stackTagCompound.setTag("attachments", attachmentTags);
		}
	}

	/** Get the melee damage of a specific gun, taking into account attachments */
	public float getMeleeDamage(ItemStack stack)
	{
		float stackMeleeDamage = meleeDamage;
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackMeleeDamage *= attachment.meleeDamageMultiplier;
		}
		return stackMeleeDamage;
	}

	/** Get the damage of a specific gun, taking into account attachments */
	public float getDamage(ItemStack stack)
	{
		float stackDamage = damage;

		if(getGrip(stack) != null && getSecondaryFire(stack))
			stackDamage = getGrip(stack).secondaryDamage;

		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackDamage *= attachment.damageMultiplier;
		}
		return stackDamage;
	}

	/** Get the bullet spread of a specific gun, taking into account attachments */
	public float getSpread(ItemStack stack, boolean sneaking, boolean sprinting)
	{
		float stackSpread = bulletSpread;

		if(getGrip(stack) != null && getSecondaryFire(stack))
			stackSpread = getGrip(stack).secondarySpread;

		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackSpread *= attachment.spreadMultiplier;
		}
		if (sprinting) {
			stackSpread *= sprintSpreadMultiplier;
		} else if (sneaking) {
			stackSpread *= sneakSpreadMultiplier;
		}
		return stackSpread;
	}

	/** Get the default spread of a specific gun, taking into account attachments */
	public float getDefaultSpread(ItemStack stack)
	{
		float stackSpread = defaultSpread;

		if(getGrip(stack) != null && getSecondaryFire(stack))
			stackSpread = getGrip(stack).secondaryDefaultSpread;

		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackSpread *= attachment.spreadMultiplier;
		}
		return stackSpread;
	}

	/** Get the recoil of a specific gun, taking into account attachments */
	public float getRecoilPitch(ItemStack stack)
	{
		float stackRecoil = this.recoilPitch + (rand.nextFloat() * this.rndRecoilPitchRange);
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackRecoil *= attachment.recoilMultiplier;
		}
		return stackRecoil;
	}
	//Used for displaying static recoil stats
	public float getRecoilDisplay(ItemStack stack)
	{
		float stackRecoil = this.recoilPitch;
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackRecoil *= attachment.recoilMultiplier;
		}
		return stackRecoil;
	}
	
	public float getRecoilYaw(ItemStack stack)
	{
		float stackRecoilYaw = this.recoilYaw + ((rand.nextFloat()-0.5F) * this.rndRecoilYawRange);
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackRecoilYaw *= attachment.recoilMultiplier;
		}
		return stackRecoilYaw;
	}
	

	/** Get the bullet speed of a specific gun, taking into account attachments */
	public float getBulletSpeed(ItemStack stack, ItemStack bulletStack)
	{
		float stackBulletSpeed = 0;
		if (bulletStack != null && bulletStack.getItem() != null) {
			stackBulletSpeed = bulletSpeed * ((ItemBullet) bulletStack.getItem()).type.speedMultiplier;
		} else {
			stackBulletSpeed = bulletSpeed;
		}

		if(getGrip(stack) != null && getSecondaryFire(stack))
			stackBulletSpeed = getGrip(stack).secondarySpeed;

		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackBulletSpeed *= attachment.bulletSpeedMultiplier;
		}
		return stackBulletSpeed;
	}
	
	/** Get the bullet speed of a specific gun, taking into account attachments */
	public float getBulletSpeed(ItemStack stack) {

		float stackBulletSpeed = bulletSpeed;

		if (getGrip(stack) != null && getSecondaryFire(stack))
			stackBulletSpeed = getGrip(stack).secondarySpeed;

		for (AttachmentType attachment : getCurrentAttachments(stack)) {
			stackBulletSpeed *= attachment.bulletSpeedMultiplier;
		}
		return stackBulletSpeed;
	}

	/** Get the reload time of a specific gun, taking into account attachments */
	public float getReloadTime(ItemStack stack)
	{
		float stackReloadTime = reloadTime;

		if(getGrip(stack) != null && getSecondaryFire(stack))
			stackReloadTime = getGrip(stack).secondaryReloadTime;

		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackReloadTime *= attachment.reloadTimeMultiplier;
		}
		return stackReloadTime;
	}
	
	/** Get the fire rate of a specific gun */
    public float getShootDelay(ItemStack stack)
    {
    	//Legacy system input as direct ticks
    	if(shootDelay != 0)
    	{
    		float fireRate = shootDelay;
            if(getGrip(stack) != null && getSecondaryFire(stack))
                fireRate = getGrip(stack).secondaryShootDelay;
            
    		return fireRate;
    	}
    	//New system, input as RPM
    	else
    	{
    		float fireRate = roundsPerMin;
    		
    		if(getGrip(stack) != null && getSecondaryFire(stack))
    			fireRate = getGrip(stack).secondaryShootDelay;
    		
    		float fireTicks = 1200 / fireRate;
    		if(fireTicks < 1)
    			fireTicks = 1;
    		
    		return fireRate = fireTicks;
    	}
	}
	
	public float getShootDelay() {
		if (shootDelay != 0) {
			return shootDelay;
		} else {
			return roundsPerMin > 1200 ? 1 : 1200/roundsPerMin;
		}
	}

	/** Get the number of bullets fired per shot of a specific gun */
	public int getNumBullets(ItemStack stack)
	{
		int amount = numBullets;

		if(getGrip(stack) != null && getSecondaryFire(stack))
			amount = getGrip(stack).secondaryNumBullets;

		return amount;
	}

	/** Get the movement speed of a specific gun, taking into account attachments */
	public float getMovementSpeed(ItemStack stack)
	{
		float stackMovement = moveSpeedModifier;
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			stackMovement *= attachment.moveSpeedMultiplier;
		}
		return stackMovement;
	}

	/** Get the recoil counter coefficient of the gun, taking into account attachments */
	public float getRecoilControl(ItemStack stack, boolean isSprinting, boolean isSneaking)
	{
		float control;
		if (isSprinting) {
			control = recoilCounterCoefficientSprinting;
		} else if (isSneaking) {
			control = recoilCounterCoefficientSneaking;
		} else {
			control = recoilCounterCoefficient;
		}

		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			if (isSprinting) {
				control *= attachment.recoilControlMultiplierSprinting;
			} else if (isSneaking){
				control *= attachment.recoilControlMultiplierSneaking;
			} else {
				control *= attachment.recoilControlMultiplier;
			}
		}

		if (control > 1) {
			return 1;
		} else if (control < 0) {
			return 0;
		} else {
			return control;
		}
	}

	public void setFireMode(ItemStack stack, int fireMode)
	{
		if(!stack.hasTagCompound())
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		
		if(fireMode < EnumFireMode.values().length)
		{
			stack.getTagCompound().setByte("GunMode", (byte)fireMode);
		}
		else
		{
			stack.getTagCompound().setByte("GunMode", (byte)mode.ordinal());
		}
	}

	/** Get the firing mode of a specific gun, taking into account attachments and secondary fire mode */
	public EnumFireMode getFireMode(ItemStack stack)
	{
		//Check for secondary fire mode
		if(getGrip(stack) != null && getSecondaryFire(stack))
			return getGrip(stack).secondaryFireMode;

		//Else check for any mode overrides from attachments
		for(AttachmentType attachment : getCurrentAttachments(stack))
		{
			if(attachment.modeOverride != null)
				return attachment.modeOverride;
		}

		//Else set the fire mode from the gun
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("GunMode"))
		{
			int gm = stack.getTagCompound().getByte("GunMode");
			if(gm < EnumFireMode.values().length)
			{
				for(int i=0; i<submode.length; i++)
				{
					if(gm == submode[i].ordinal())
					{
						return EnumFireMode.values()[gm];
					}
				}
			}
		}

		setFireMode(stack, mode.ordinal());
		return mode;
	}

	public String getGunBox() {
		if (gunBoxName != "") {
			return gunBoxName;
		} else {
			for (BlockGunBox box : FlansMod.gunBoxBlocks) {
				if (box.searchFor(this) != null) {
					gunBoxName = box.getLocalizedName();
					return gunBoxName;
				}
			}
			gunBoxName = "none";
			return gunBoxName;
		}
	}

	/** Set the secondary or primary fire mode */
	public void setSecondaryFire(ItemStack stack, boolean mode)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.stackTagCompound.setBoolean("secondaryFire", mode);
	}

	/** Get whether the gun is in secondary or primary fire mode */
	public boolean getSecondaryFire(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		if(!stack.getTagCompound().hasKey("secondaryFire"))
		{
			stack.stackTagCompound.setBoolean("secondaryFire", false);
			return stack.getTagCompound().getBoolean("secondaryFire");
		}

		return stack.getTagCompound().getBoolean("secondaryFire");
	}

	/** Get the max size of ammo items depending on what mode the gun is in */
	public int getNumAmmoItemsInGun(ItemStack stack)
	{
		if(getGrip(stack) != null && getSecondaryFire(stack))
			return getGrip(stack).numSecAmmoItems;
		else
			return numPrimaryAmmoItems;
	}

	/** Static String to GunType method */
	public static GunType getGun(String s)
	{
		return guns.get(s);
	}

	public Paintjob getPaintjob(String s)
	{
		for(Paintjob paintjob : paintjobs)
		{
			if(paintjob.iconName.equals(s))
				return paintjob;
		}
		return defaultPaintjob;
	}
	
	@Override
	public float GetRecommendedScale()
	{
		return 60.0f;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBase GetModel() 
	{
		return model;
	}
}