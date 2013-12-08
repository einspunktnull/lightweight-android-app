package net.einspunktnull.leightweight.daogenerator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * 
 * @author Albrecht Nitsche
 */
public class LightweightDaoGenerator
{

	public static void main(String[] args) throws Exception
	{
		LightweightDaoGenerator gen = new LightweightDaoGenerator();
		gen.generate(1, "net.einspunktnull.lightweight.greenDao", "../lightweight-android-project/src-gen");
	}

	private Schema schema;
	private DaoGenerator generator;

	public LightweightDaoGenerator() throws Exception
	{
		generator = new DaoGenerator();
	}

	public void generate(int version, String pckg, String directory) throws Exception
	{
		schema = new Schema(version, pckg);
		schema.enableKeepSectionsByDefault();
		addTables();
		generator.generateAll(schema, directory);
	}

	private void addTables()
	{
		Entity artist = schema.addEntity("Entry");
		artist.addIdProperty().autoincrement();
		artist.addLongProperty("date").index().notNull();
		artist.addIntProperty("carb").notNull();
		artist.addIntProperty("fat").notNull();
		artist.addIntProperty("water").notNull();
		artist.addIntProperty("sport").notNull();
		artist.addFloatProperty("weight").notNull();

	}

}
