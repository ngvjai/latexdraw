package net.sf.latexdraw.parsers.pst.parser

import scala.collection.JavaConversions._
import net.sf.latexdraw.glib.models.interfaces.DrawingTK
import net.sf.latexdraw.glib.models.interfaces.IGroup
import net.sf.latexdraw.glib.models.interfaces.IShape
import net.sf.latexdraw.glib.models.interfaces.IFreehand
import scala.util.parsing.input.CharArrayReader
import net.sf.latexdraw.glib.models.interfaces.IText
import java.awt.Color
import net.sf.latexdraw.glib.views.latex.DviPsColors

/**
 * Defines a parser parsing PST expressions.<br>
 *<br>
 * This file is part of LaTeXDraw<br>
 * Copyright (c) 2005-2013 Arnaud BLOUIN<br>
 *<br>
 *  LaTeXDraw is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.<br>
 *<br>
 *  LaTeXDraw is distributed without any warranty; without even the
 *  implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 *  PURPOSE. See the GNU General Public License for more details.<br>
 *<br>
 * 2012-04-24<br>
 * @author Arnaud BLOUIN
 * @version 3.0
 */
trait PSTCodeParser extends PSTAbstractParser
	with PSFrameEllipseDiamondTriangleParser with PSCircleParser with PSLineParser with PSPolygonParser with PSWedgeArcParser
	with PSBezierParser with PSCurveParabolaParser with PSDotParser with PSGridAxes with PSTPlotParser with PSCustomParser
	with TextCommandsParser with PSFrameboxParser with IPSTCodeParser {

	override def parsePSTCode(ctx : PSTContext) : Parser[IGroup] =
		rep(parsePSTBlock(ctx, ctx.isPsCustom) | parsePspictureBlock(ctx) | parsePsset(ctx) |
			parsePsellipse(new PSTContext(ctx)) | parsePsframe(new PSTContext(ctx)) |
			parsePsdiamond(new PSTContext(ctx)) | parsePstriangle(new PSTContext(ctx)) |
			parsePsline(new PSTContext(ctx)) | parserQline(new PSTContext(ctx)) |
			parsePscircle(new PSTContext(ctx)) | parseQdisk(new PSTContext(ctx)) |
			parsePspolygon(new PSTContext(ctx)) | parsePsbezier(new PSTContext(ctx)) |
			parsePsdot(new PSTContext(ctx)) | parsePsdots(new PSTContext(ctx)) |
			parsePsgrid(new PSTContext(ctx)) | parseRput(ctx) |
			parsePswedge(new PSTContext(ctx)) | parsePsarc(new PSTContext(ctx)) | parsePsarcn(new PSTContext(ctx)) |
			parsePsellipticarc(new PSTContext(ctx)) | parsePsellipticarcn(new PSTContext(ctx)) |
			parseParabola(new PSTContext(ctx)) | parsePscurve(new PSTContext(ctx)) | parsePsecurve(new PSTContext(ctx)) |
			parsePsccurve(new PSTContext(ctx)) | parsePSTPlotCommands(new PSTContext(ctx)) | parseNewpsobject(ctx) | parseNewpsstyle(ctx) |
			parsePscustom(new PSTContext(ctx)) | parseDefineColor(ctx) |
			parsePSCustomCommands(ctx) | parsePsFrameboxCmds(ctx) | parsetextCommands(ctx) | parseText(ctx)) ^^ {
		case list =>
		val group = DrawingTK.getFactory.createGroup(false)

		list.foreach{_ match {
				case gp : List[_] => gp.foreach{sh => group.addShape(sh.asInstanceOf[IShape])}
				case gp : IGroup => gp.getShapes.foreach{sh => group.addShape(sh)}
				case sh : IShape => group.addShape(sh)
				case _ =>
			}
		}

		group
	}



	override def parseText(ctx : PSTContext) : Parser[List[IShape]] =  (math | text | ident | numeric | commandUnknown) ^^ {
		case obj =>
			ctx.textParsed match {
				case "" => ctx.textParsed = obj.mkString
				case _  => ctx.textParsed += " " + obj.mkString
			}
			Nil
	}


	override def parsePscustom(ctx : PSTContext) : Parser[IGroup] = ("\\pscustom*" | "\\pscustom") ~ opt(parseParam(ctx)) ~ parsePSTBlock(ctx, true) ^^ {
		case cmdName ~ _ ~ shapes => shapes
			if(cmdName.endsWith("*"))
				shapes.getShapes.foreach{sh => setShapeForStar(sh)}

			var fh : IFreehand = null
			val gp = DrawingTK.getFactory.createGroup(false)

			// The different created freehand shapes must be merged into a single one.
			shapes.getShapes.foreach{sh =>
				sh match {
					case ifh : IFreehand =>
						fh match {
							case null => gp.addShape(ifh) ; fh = ifh // This shape is now the reference shape used for the merge.
							case _ =>
								if(ifh.getNbPoints==1) {// If the shape has a single point, it means it is a closepath command
									fh.setOpen(ifh.isOpen)
								} else {// Otherwise, the shape has two points. So, we take the last one and add it to the first shape.
									fh.addPoint(ifh.getPtAt(ifh.getNbPoints-1))
									fh.setType(ifh.getType)
								}
						}
					case _ => gp.addShape(sh)
				}
			}
			gp
	}


	override def parseNewpsobject(ctx : PSTContext) : Parser[List[IShape]] = "\\newpsobject" ~ parseBracket(ctx) ~ parseBracket(ctx) ~ parseBracket(ctx) ^^ {
		case _ ~ name ~ obj ~ attributes => PSTParser.errorLogs += "The command newpsobject is not supported yet" ; Nil
	}


	override def parseNewpsstyle(ctx : PSTContext) : Parser[List[IShape]] = "\\newpsstyle" ~ parseBracket(ctx) ~ parseBracket(ctx) ^^ {
		case _ ~ name ~ attributes => PSTParser.errorLogs += "The command newpsstyle is not supported yet" ; Nil
	}


	/** Parses a PST block surrounded with brackets. */
	override def parsePSTBlock(ctx : PSTContext, isPsCustomBlock : Boolean) : Parser[IGroup] = {
		val newCtx = new PSTContext(ctx, isPsCustomBlock)
		"{" ~ parsePSTCode(newCtx) ~ "}" ^^ {
			case _ ~ shapes ~ _ =>
			shapes.getShapes.addAll(0, checkTextParsed(newCtx))
			shapes
		}
	}


	override def parseDefineColor(ctx:PSTContext) : Parser[Unit] = "\\definecolor" ~ parseBracket(ctx) ~ parseBracket(ctx) ~ parseBracket(ctx) ^^ {
		case _ ~ colName ~ colType ~ colSpec =>
			var colour : Color = null
			try{
				colType match {
					case "rgb" => colSpec.split(',') match {
							case Array(r,g,b) => colour = new Color(r.toFloat, g.toFloat, b.toFloat)
							case _ => PSTParser.errorLogs += "An rgb colour must have 3 numbers."
						}
					case "RGB" => colSpec.split(',') match {
							case Array(r,g,b) => colour = DviPsColors.INSTANCE.convertRGB2rgb(r.toDouble, g.toDouble, b.toDouble)
							case _ => PSTParser.errorLogs += "An RGB colour must have 3 numbers."
						}
					case "gray" => colour = DviPsColors.INSTANCE.convertgray2rgb(colSpec.toDouble)
					case "HTML" => colour = DviPsColors.INSTANCE.convertHTML2rgb(colSpec)
					case "cmyk" => colSpec.split(',') match {
							case Array(c,m,y,k) => colour = DviPsColors.INSTANCE.convertcmyk2rgb(c.toDouble, m.toDouble, y.toDouble, k.toDouble)
							case _ => PSTParser.errorLogs += "An cmyk colour must have 3 numbers."
						}
					case "cmy" => colSpec.split(',') match {
							case Array(c,m,y) => colour = new Color(1-c.toFloat, 1-m.toFloat, 1-y.toFloat)
							case _ => PSTParser.errorLogs += "An cmy colour must have 3 numbers."
						}
					case "hsb" => colSpec.split(',') match {
							case Array(h,s,b) => colour = new Color(Color.HSBtoRGB(h.toFloat, s.toFloat, b.toFloat))
							case _ => PSTParser.errorLogs += "An hsb colour must have 3 numbers."
						}
					case _ => PSTParser.errorLogs += "Unknown color type: " + colType
				}
			}catch{case e => PSTParser.errorLogs += "Error during colour conversion: " + colName + " " + colType + " " + colSpec + " " + e.getStackTraceString }

			if(colour!=null)
				DviPsColors.INSTANCE.addUserColour(colour, colName)
	}


	override def parseRput(ctx : PSTContext) : Parser[IGroup] = {
		val ctx2 = new PSTContext(ctx, ctx.isPsCustom)// Must create an other context not to modify the current one.
		("\\rput*" | "\\rput") ~ opt(parseRputTextPosition(ctx2)) ~ opt(parseRputRotationAngle(ctx2)) ~
		parseCoord(ctx2) ~ parsePSTBlock(ctx2, false) ^^ { case _ ~ _ ~ _ ~ coord ~ figs =>
			figs.getShapes.foreach{shape => shape.translate(coord.getX * IShape.PPC, -coord.getY * IShape.PPC) }
			figs
		}
	}


	private def parseRputRotationAngle(ctx : PSTContext) : Parser[Unit] = parseBracket(ctx) ^^ {
		case rotation => parseValuePutRotation(rotation) match {
			case Some(Tuple2(rotationAngle, true)) => ctx.rputAngle += rotationAngle
			case Some(Tuple2(rotationAngle, false)) => ctx.rputAngle = rotationAngle
			case _ =>
		}
	}


	private def parseRputTextPosition(ctx : PSTContext) : Parser[Unit] = parseSquaredBracket(ctx) ^^ {
		case refPos => ctx.textPosition = refPos
	}



	override def parsePspictureBlock(ctx : PSTContext) : Parser[IGroup] = {
		val ctx2 = new PSTContext(ctx, false)
		(parseBeginPspicture(ctx2, false) ~> parsePSTCode(ctx2) <~ "\\end" <~ "{" <~ "pspicture" <~ "}") |
		(parseBeginPspicture(ctx2, true) ~> parsePSTCode(ctx2) <~ "\\end" <~ "{" <~ "pspicture*" <~ "}")
	}


	/**
	 * Parses begin{pspicture} commands.
	 */
	private def parseBeginPspicture(ctx : PSTContext, star : Boolean) : Parser[Any] =
		"\\begin" ~> "{" ~> (if(star) "pspicture*" else "pspicture") ~> "}" ~> parseCoord(ctx) ~ opt(parseCoord(ctx)) ^^ {
		case p1 ~ p2 =>
		p2 match {
			case Some(value) =>
				ctx.pictureSWPt = p1
				ctx.pictureNEPt = value
			case _ =>
				ctx.pictureSWPt = DrawingTK.getFactory.createPoint
				ctx.pictureNEPt = p1
		}
	}
}